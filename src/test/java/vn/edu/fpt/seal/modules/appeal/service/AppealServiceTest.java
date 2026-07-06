package vn.edu.fpt.seal.modules.appeal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import vn.edu.fpt.seal.common.enums.AppealStatus;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.appeal.dto.AppealResponse;
import vn.edu.fpt.seal.modules.appeal.dto.CreateAppealRequest;
import vn.edu.fpt.seal.modules.appeal.entity.Appeal;
import vn.edu.fpt.seal.modules.appeal.repository.AppealRepository;
import vn.edu.fpt.seal.modules.event.entity.Event;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.round.repository.RoundRepository;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.team.entity.TeamMember;
import vn.edu.fpt.seal.modules.team.repository.TeamMemberRepository;
import vn.edu.fpt.seal.modules.teamtimeline.service.TeamTimelineService;
import vn.edu.fpt.seal.modules.track.entity.Track;
import vn.edu.fpt.seal.modules.user.entity.User;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;
import vn.edu.fpt.seal.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test trọng tâm cho quy tắc "cửa sổ khiếu nại 15 phút":
 * - Nộp trong cửa sổ -> thành công.
 * - Nộp sau hạn chót -> backend TỪ CHỐI (đây là quy tắc quan trọng nhất).
 * - Chưa công bố kết quả -> từ chối.
 */
@ExtendWith(MockitoExtension.class)
class AppealServiceTest {

    @Mock private AppealRepository appealRepository;
    @Mock private RoundRepository roundRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamTimelineService timelineService;
    @Mock private Authentication auth;

    @InjectMocks private AppealService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID roundId = UUID.randomUUID();
    private Round round;
    private Team team;
    private User user;

    @BeforeEach
    void setUp() {
        // Dựng cây dữ liệu event -> track -> round/team dùng chung cho các test
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("SEAL Hackathon");

        Track track = new Track();
        track.setId(UUID.randomUUID());
        track.setEvent(event);
        track.setName("AI Track");

        round = new Round();
        round.setId(roundId);
        round.setTrack(track);
        round.setName("Round 1");

        team = new Team();
        team.setId(UUID.randomUUID());
        team.setTrack(track);
        team.setName("Team Alpha");

        user = User.builder().build();
        user.setId(userId);
        user.setFullName("Nguyen Van A");
    }

    /** Giả lập user đã đăng nhập là thành viên của team trong track của vòng. */
    private void stubAuthenticatedMember() {
        when(auth.getPrincipal()).thenReturn(CurrentUser.builder().id(userId).build());
        TeamMember member = TeamMember.builder().team(team).user(user).build();
        when(teamMemberRepository.findByUserIdOrderByJoinedAtDesc(userId)).thenReturn(List.of(member));
    }

    @Test
    void create_withinAppealWindow_succeeds() {
        // Cửa sổ khiếu nại còn mở (hạn chót ở tương lai)
        round.setResultPublishedAt(LocalDateTime.now().minusMinutes(5));
        round.setAppealDeadline(LocalDateTime.now().plusMinutes(10));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        stubAuthenticatedMember();
        when(appealRepository.existsByRoundIdAndTeamIdAndStatus(roundId, team.getId(), AppealStatus.PENDING)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appealRepository.save(any(Appeal.class))).thenAnswer(inv -> inv.getArgument(0));

        AppealResponse res = service.create(new CreateAppealRequest(roundId, "Score seems wrong"), auth);

        assertThat(res.status()).isEqualTo(AppealStatus.PENDING);
        assertThat(res.teamId()).isEqualTo(team.getId());
        // Snapshot cửa sổ phải được lưu lại trên đơn khiếu nại
        assertThat(res.appealDeadline()).isEqualTo(round.getAppealDeadline());
        verify(appealRepository).save(any(Appeal.class));
        verify(timelineService).record(eq(team), eq(round), any(), any(), any());
    }

    @Test
    void create_afterDeadline_isRejected() {
        // Hạn chót đã qua -> backend phải từ chối bất kể FE hiển thị gì
        round.setResultPublishedAt(LocalDateTime.now().minusMinutes(30));
        round.setAppealDeadline(LocalDateTime.now().minusMinutes(15));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        // Caller đã đăng nhập; điều bị từ chối phải là do quá hạn, không phải do thiếu auth
        when(auth.getPrincipal()).thenReturn(CurrentUser.builder().id(userId).build());

        assertThatThrownBy(() -> service.create(new CreateAppealRequest(roundId, "Too late"), auth))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("appeal window has closed");
        verify(appealRepository, never()).save(any());
    }

    @Test
    void create_beforeResultsPublished_isRejected() {
        // Chưa công bố kết quả -> chưa có gì để khiếu nại
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        // Caller đã đăng nhập; điều bị từ chối phải là do chưa công bố kết quả
        when(auth.getPrincipal()).thenReturn(CurrentUser.builder().id(userId).build());

        assertThatThrownBy(() -> service.create(new CreateAppealRequest(roundId, "Nothing published"), auth))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("have not been published");
        verify(appealRepository, never()).save(any());
    }

    @Test
    void create_duplicatePendingAppeal_isRejected() {
        // Mỗi đội chỉ được một đơn PENDING cho một vòng
        round.setResultPublishedAt(LocalDateTime.now().minusMinutes(1));
        round.setAppealDeadline(LocalDateTime.now().plusMinutes(14));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        stubAuthenticatedMember();
        when(appealRepository.existsByRoundIdAndTeamIdAndStatus(roundId, team.getId(), AppealStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateAppealRequest(roundId, "Again"), auth))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already has a pending appeal");
    }

    @Test
    void publishResults_opens15MinuteWindow() {
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));

        service.publishResults(roundId);

        // Hạn chót = thời điểm công bố + đúng 15 phút
        assertThat(round.getResultPublishedAt()).isNotNull();
        assertThat(round.getAppealDeadline())
                .isEqualTo(round.getResultPublishedAt().plusMinutes(AppealService.APPEAL_WINDOW_MINUTES));
    }
}
