package vn.edu.fpt.seal.modules.appeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.AppealStatus;
import vn.edu.fpt.seal.common.enums.TimelineEventType;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.appeal.dto.*;
import vn.edu.fpt.seal.modules.appeal.entity.Appeal;
import vn.edu.fpt.seal.modules.appeal.mapper.AppealMapper;
import vn.edu.fpt.seal.modules.appeal.repository.AppealRepository;
import vn.edu.fpt.seal.modules.round.dto.RoundResponse;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.round.mapper.RoundMapper;
import vn.edu.fpt.seal.modules.round.repository.RoundRepository;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.team.entity.TeamMember;
import vn.edu.fpt.seal.modules.team.repository.TeamMemberRepository;
import vn.edu.fpt.seal.modules.teamtimeline.service.TeamTimelineService;
import vn.edu.fpt.seal.modules.user.entity.User;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;
import vn.edu.fpt.seal.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý khiếu nại kết quả vòng thi với "cửa sổ 15 phút":
 * - EC công bố kết quả -> mở cửa sổ khiếu nại 15 phút (publishResults).
 * - Thí sinh (leader/member của đội) chỉ nộp được khiếu nại TRƯỚC hạn chót — backend luôn kiểm tra.
 * - Điều phối viên phản hồi / chấp nhận / từ chối; khi resolve sẽ ghi mốc vào timeline của đội.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppealService {

    /** Độ dài cửa sổ khiếu nại tính từ lúc công bố kết quả (phút). */
    public static final int APPEAL_WINDOW_MINUTES = 15;

    private final AppealRepository appealRepository;
    private final RoundRepository roundRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamTimelineService timelineService;

    /**
     * EC công bố kết quả vòng thi và mở cửa sổ khiếu nại 15 phút.
     * Cho phép công bố lại (re-publish) — cửa sổ sẽ được đặt lại từ thời điểm mới.
     */
    @Transactional
    public RoundResponse publishResults(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> ApiException.notFound("Round not found: " + roundId));
        LocalDateTime now = LocalDateTime.now();
        round.setResultPublishedAt(now);
        round.setAppealDeadline(now.plusMinutes(APPEAL_WINDOW_MINUTES));
        log.info("Round results published: round={}, appealDeadline={}", roundId, round.getAppealDeadline());
        return RoundMapper.toResponse(round);
    }

    /**
     * Thí sinh nộp khiếu nại. Ràng buộc:
     * - Kết quả vòng phải đã được công bố.
     * - Thời điểm hiện tại phải TRƯỚC hạn chót khiếu nại (cửa sổ 15 phút).
     * - Người nộp phải là thành viên của một đội thuộc track của vòng thi.
     * - Mỗi đội chỉ có tối đa một khiếu nại PENDING cho một vòng.
     */
    @Transactional
    public AppealResponse create(CreateAppealRequest req, Authentication auth) {
        UUID callerId = currentUserId(auth);
        Round round = roundRepository.findById(req.roundId())
                .orElseThrow(() -> ApiException.notFound("Round not found: " + req.roundId()));

        // Chưa công bố kết quả thì chưa có gì để khiếu nại
        if (round.getResultPublishedAt() == null || round.getAppealDeadline() == null) {
            throw ApiException.badRequest("Round results have not been published yet");
        }
        // Quy tắc cốt lõi: từ chối mọi khiếu nại nộp sau hạn chót
        if (LocalDateTime.now().isAfter(round.getAppealDeadline())) {
            throw ApiException.badRequest("The appeal window has closed (deadline: " + round.getAppealDeadline() + ")");
        }

        Team team = resolveCallerTeamInTrack(callerId, round);
        if (appealRepository.existsByRoundIdAndTeamIdAndStatus(round.getId(), team.getId(), AppealStatus.PENDING)) {
            throw ApiException.conflict("Your team already has a pending appeal for this round");
        }

        User submitter = userRepository.findById(callerId)
                .orElseThrow(() -> ApiException.notFound("User not found: " + callerId));

        Appeal appeal = appealRepository.save(Appeal.builder()
                .event(round.getTrack().getEvent())
                .round(round)
                .team(team)
                .submittedBy(submitter)
                .reason(req.reason().trim())
                .status(AppealStatus.PENDING)
                // Snapshot thông tin cửa sổ tại thời điểm nộp — phục vụ audit về sau
                .resultPublishedAt(round.getResultPublishedAt())
                .appealDeadline(round.getAppealDeadline())
                .build());

        // Ghi mốc "đã nộp khiếu nại" vào hành trình của đội
        timelineService.record(team, round, TimelineEventType.APPEAL_SUBMITTED,
                "Appeal submitted",
                "Appeal submitted for round '" + round.getName() + "': " + appeal.getReason());
        log.info("Appeal created: id={}, round={}, team={}", appeal.getId(), round.getId(), team.getId());
        return AppealMapper.toResponse(appeal);
    }

    /** Điều phối viên tìm kiếm/duyệt danh sách khiếu nại. */
    @Transactional(readOnly = true)
    public Page<AppealResponse> search(UUID eventId, UUID roundId, AppealStatus status, Pageable pageable) {
        return appealRepository.search(eventId, roundId, status, pageable).map(AppealMapper::toResponse);
    }

    /** Thí sinh xem các khiếu nại của đội mình. */
    @Transactional(readOnly = true)
    public List<AppealResponse> byTeam(UUID teamId, Authentication auth) {
        if (!isCoordinator(auth) && !teamMemberRepository.existsByTeamIdAndUserId(teamId, currentUserId(auth))) {
            throw ApiException.forbidden("You can only view appeals of your own team");
        }
        return appealRepository.findByTeamIdOrderByCreatedAtDesc(teamId).stream()
                .map(AppealMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AppealResponse get(UUID id) {
        return AppealMapper.toResponse(findOrThrow(id));
    }

    /** EC ghi phản hồi trung gian (đơn vẫn ở trạng thái PENDING). */
    @Transactional
    public AppealResponse respond(UUID id, RespondAppealRequest req) {
        Appeal appeal = findOrThrow(id);
        ensurePending(appeal);
        appeal.setResponse(req.response().trim());
        return AppealMapper.toResponse(appeal);
    }

    /**
     * EC chốt kết luận: ACCEPTED hoặc REJECTED. Ghi mốc APPEAL_RESOLVED vào timeline.
     */
    @Transactional
    public AppealResponse resolve(UUID id, ResolveAppealRequest req, Authentication auth) {
        Appeal appeal = findOrThrow(id);
        ensurePending(appeal);
        if (req.status() != AppealStatus.ACCEPTED && req.status() != AppealStatus.REJECTED) {
            throw ApiException.badRequest("Resolution status must be ACCEPTED or REJECTED");
        }
        UUID resolverId = currentUserId(auth);
        appeal.setStatus(req.status());
        if (req.response() != null && !req.response().isBlank()) appeal.setResponse(req.response().trim());
        appeal.setResolvedBy(userRepository.findById(resolverId)
                .orElseThrow(() -> ApiException.notFound("User not found: " + resolverId)));
        appeal.setResolvedAt(LocalDateTime.now());

        String outcome = req.status() == AppealStatus.ACCEPTED ? "accepted" : "rejected";
        timelineService.record(appeal.getTeam(), appeal.getRound(), TimelineEventType.APPEAL_RESOLVED,
                "Appeal " + outcome,
                "Appeal for round '" + appeal.getRound().getName() + "' was " + outcome
                        + (appeal.getResponse() == null ? "" : ". Response: " + appeal.getResponse()),
                null, null, appeal.getStatus().name());
        log.info("Appeal resolved: id={}, status={}, by={}", id, req.status(), resolverId);
        return AppealMapper.toResponse(appeal);
    }

    // ==== Helpers ====

    /** Tìm đội (active) của người gọi thuộc đúng track của vòng thi. */
    private Team resolveCallerTeamInTrack(UUID callerId, Round round) {
        UUID trackId = round.getTrack().getId();
        for (TeamMember m : teamMemberRepository.findByUserIdOrderByJoinedAtDesc(callerId)) {
            if (m.getTeam().getTrack().getId().equals(trackId)) return m.getTeam();
        }
        throw ApiException.forbidden("You are not a member of any team in this round's track");
    }

    private Appeal findOrThrow(UUID id) {
        return appealRepository.findWithRelationsById(id)
                .orElseThrow(() -> ApiException.notFound("Appeal not found: " + id));
    }

    private void ensurePending(Appeal appeal) {
        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw ApiException.badRequest("Appeal has already been resolved (status: " + appeal.getStatus() + ")");
        }
    }

    private UUID currentUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof CurrentUser c) return c.getId();
        throw ApiException.forbidden("Authentication required");
    }

    private boolean isCoordinator(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDINATOR"));
    }
}
