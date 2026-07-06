package vn.edu.fpt.seal.modules.teamtimeline.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.TimelineEventType;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.team.entity.TeamMember;
import vn.edu.fpt.seal.modules.team.repository.TeamMemberRepository;
import vn.edu.fpt.seal.modules.teamtimeline.dto.TeamTimelineEventResponse;
import vn.edu.fpt.seal.modules.teamtimeline.entity.TeamTimelineEvent;
import vn.edu.fpt.seal.modules.teamtimeline.mapper.TeamTimelineEventMapper;
import vn.edu.fpt.seal.modules.teamtimeline.repository.TeamTimelineEventRepository;
import vn.edu.fpt.seal.security.CurrentUser;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service quản lý "hành trình của đội" (Team Journey Timeline).
 * Các module khác (team, ranking, appeal, prize) gọi record(...) để ghi mốc;
 * service này chỉ phụ thuộc vào repository của chính nó nên không gây vòng lặp bean.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamTimelineService {

    private final TeamTimelineEventRepository timelineRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * Ghi một mốc mới vào timeline của đội. Event (hackathon) được suy ra
     * từ team.track.event nên caller không cần truyền riêng.
     * Các tham số snapshot (score/rank/status) có thể null nếu không áp dụng.
     */
    @Transactional
    public void record(Team team, Round round, TimelineEventType type, String title, String description,
                       BigDecimal scoreSnapshot, Integer rankSnapshot, String statusSnapshot) {
        timelineRepository.save(TeamTimelineEvent.builder()
                .event(team.getTrack().getEvent())
                .team(team)
                .round(round)
                .type(type)
                .title(title)
                .description(description)
                .scoreSnapshot(scoreSnapshot)
                .rankSnapshot(rankSnapshot)
                .statusSnapshot(statusSnapshot)
                .build());
        log.debug("Timeline event recorded: team={}, type={}", team.getId(), type);
    }

    /** Bản rút gọn cho các mốc không cần snapshot điểm/hạng. */
    @Transactional
    public void record(Team team, Round round, TimelineEventType type, String title, String description) {
        record(team, round, type, title, description, null, null, null);
    }

    /**
     * Timeline cho các đội của người dùng hiện tại (trang "Team Journey" của thí sinh).
     * Nếu truyền eventId thì chỉ trả về mốc thuộc sự kiện đó.
     */
    @Transactional(readOnly = true)
    public List<TeamTimelineEventResponse> myTeamTimeline(Authentication auth, UUID eventId) {
        UUID callerId = currentUserId(auth);
        List<TeamTimelineEventResponse> result = new ArrayList<>();
        Set<UUID> seenTeams = new HashSet<>();
        for (TeamMember m : teamMemberRepository.findByUserIdOrderByJoinedAtDesc(callerId)) {
            Team team = m.getTeam();
            if (!seenTeams.add(team.getId())) continue; // tránh trùng đội
            if (eventId != null && !team.getTrack().getEvent().getId().equals(eventId)) continue;
            timelineRepository.findByTeamIdOrderByOccurredAtDesc(team.getId())
                    .forEach(e -> result.add(TeamTimelineEventMapper.toResponse(e)));
        }
        // Sắp xếp lại toàn bộ theo thời gian giảm dần (trường hợp user có nhiều đội)
        result.sort(Comparator.comparing(TeamTimelineEventResponse::occurredAt).reversed());
        return result;
    }

    /**
     * Timeline của một đội cụ thể. Điều phối viên xem được mọi đội;
     * thí sinh chỉ xem được đội mà mình là thành viên.
     */
    @Transactional(readOnly = true)
    public List<TeamTimelineEventResponse> teamTimeline(UUID teamId, Authentication auth) {
        if (!isCoordinator(auth) && !teamMemberRepository.existsByTeamIdAndUserId(teamId, currentUserId(auth))) {
            throw ApiException.forbidden("You can only view the timeline of your own team");
        }
        return timelineRepository.findByTeamIdOrderByOccurredAtDesc(teamId).stream()
                .map(TeamTimelineEventMapper::toResponse)
                .toList();
    }

    /** Timeline của mọi đội trong một sự kiện — dành cho điều phối viên (EC). */
    @Transactional(readOnly = true)
    public Page<TeamTimelineEventResponse> eventTimeline(UUID eventId, Pageable pageable) {
        return timelineRepository.findByEventIdOrderByOccurredAtDesc(eventId, pageable)
                .map(TeamTimelineEventMapper::toResponse);
    }

    // ==== Các helper lấy user hiện tại — theo cùng pattern với TeamService ====
    private UUID currentUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof CurrentUser c) return c.getId();
        throw ApiException.forbidden("Authentication required");
    }

    private boolean isCoordinator(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDINATOR"));
    }
}
