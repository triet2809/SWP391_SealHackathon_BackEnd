package vn.edu.fpt.seal.modules.ranking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.AppealStatus;
import vn.edu.fpt.seal.common.enums.PromotionStatus;
import vn.edu.fpt.seal.common.enums.TimelineEventType;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.appeal.repository.AppealRepository;
import vn.edu.fpt.seal.modules.criteria.repository.RoundCriterionRepository;
import vn.edu.fpt.seal.modules.ranking.dto.*;
import vn.edu.fpt.seal.modules.ranking.entity.RoundRanking;
import vn.edu.fpt.seal.modules.ranking.entity.TieBreakDecision;
import vn.edu.fpt.seal.modules.ranking.mapper.RoundRankingMapper;
import vn.edu.fpt.seal.modules.ranking.repository.RoundRankingRepository;
import vn.edu.fpt.seal.modules.ranking.repository.TieBreakDecisionRepository;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.round.repository.RoundRepository;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.team.repository.TeamRepository;
import vn.edu.fpt.seal.modules.teamtimeline.service.TeamTimelineService;
import vn.edu.fpt.seal.modules.user.entity.User;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;
import vn.edu.fpt.seal.security.CurrentUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoundRankingService {
    private final RoundRankingRepository rankingRepository;
    private final RoundRepository roundRepository;
    private final TeamRepository teamRepository;
    private final RoundCriterionRepository criterionRepository;
    private final AppealRepository appealRepository;
    private final TieBreakDecisionRepository tieBreakDecisionRepository;
    private final UserRepository userRepository;
    private final TeamTimelineService timelineService;

    @Transactional(readOnly = true)
    public Page<RoundRankingResponse> list(UUID roundId, Pageable pageable) {
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("rank").ascending());
        return rankingRepository.findByRoundId(roundId, effectivePageable).map(RoundRankingMapper::toResponse);
    }

    /**
     * Tính lại xếp hạng của một vòng thi.
     *
     * Thứ tự phân định khi các đội HÒA tổng điểm (tie-break):
     *   1. Quyết định thủ công (review mã nguồn GitHub) — đội có quyết định xếp trên.
     *   2. Điểm trên tiêu chí có trọng số cao nhất (logic sẵn có).
     *   3. Đội ít thành viên hơn xếp trên.
     *   4. Đội nộp bài sớm hơn xếp trên.
     *   5. Tên đội (chỉ để thứ tự ổn định/deterministic).
     *
     * Khi applyPromotion=true: bị CHẶN nếu cửa sổ khiếu nại còn mở hoặc còn
     * khiếu nại chưa xử lý — đảm bảo không thăng hạng khi kết quả còn tranh chấp.
     */
    @Transactional
    public List<RoundRankingResponse> recalculate(UUID roundId, RecalculateRankingsRequest req) {
        Round round = roundRepository.findById(roundId).orElseThrow(() -> ApiException.notFound("Round not found: " + roundId));
        boolean applyPromotion = req != null && Boolean.TRUE.equals(req.applyPromotion());

        // Chặn thăng hạng khi kết quả còn có thể bị khiếu nại hoặc đang tranh chấp
        if (applyPromotion) {
            ensureNoOpenAppealWindow(round);
            ensureNoUnresolvedAppeals(roundId);
        }

        List<RoundRankingRepository.RoundScoreRow> rows = rankingRepository.calculateRows(roundId);

        // Bộ phân định hòa: gom đủ dữ liệu (điểm theo tiêu chí, quyết định thủ công,
        // số thành viên, thời điểm nộp bài) để so sánh và giải thích lý do.
        TieBreaker tieBreaker = new TieBreaker(
                rankingRepository.criterionScores(roundId),
                tieBreakDecisionRepository.findByRoundId(roundId),
                rankingRepository.memberCounts(roundId),
                rankingRepository.submissionTimes(roundId));

        // Sắp xếp: tổng điểm giảm dần, sau đó lần lượt các luật tie-break ở trên.
        List<RoundRankingRepository.RoundScoreRow> ordered = new ArrayList<>(rows);
        ordered.sort(
                Comparator.<RoundRankingRepository.RoundScoreRow, BigDecimal>comparing(
                                r -> nz(r.getTotalScore()), Comparator.reverseOrder())
                        .thenComparing(tieBreaker::compareByManualDecision)
                        .thenComparing(tieBreaker::compareByCriterion)
                        .thenComparing(tieBreaker::compareByMemberCount)
                        .thenComparing(tieBreaker::compareBySubmissionTime)
                        .thenComparing(r -> r.getTeamName() == null ? "" : r.getTeamName()));

        rankingRepository.deleteByRoundId(roundId);
        rankingRepository.flush();

        int topN = round.getTopNToPromote() == null ? 0 : round.getTopNToPromote();
        List<RoundRanking> saved = new ArrayList<>();
        int rank = 1;
        BigDecimal prevTotal = null;
        UUID prevTeamId = null;
        for (RoundRankingRepository.RoundScoreRow row : ordered) {
            Team team = teamRepository.findById(row.getTeamId()).orElseThrow(() -> ApiException.notFound("Team not found: " + row.getTeamId()));
            PromotionStatus status = PromotionStatus.pending;
            if (applyPromotion) status = rank <= topN ? PromotionStatus.promoted : PromotionStatus.eliminated;

            // Chỉ ghi lý do tie-break khi đội này thực sự hòa tổng điểm với đội xếp ngay trên,
            // để dữ liệu giải thích được VÌ SAO thứ tự như vậy.
            boolean tiedWithPrev = prevTotal != null && nz(row.getTotalScore()).compareTo(prevTotal) == 0;
            TieBreaker.Outcome outcome = tiedWithPrev ? tieBreaker.explain(prevTeamId, row.getTeamId()) : null;

            RoundRanking.RoundRankingBuilder builder = RoundRanking.builder()
                    .round(round)
                    .team(team)
                    .totalScore(row.getTotalScore())
                    .rank(rank)
                    .status(status);
            if (outcome != null) {
                if (outcome.criterionId() != null) {
                    builder.tieBreakerCriterion(criterionRepository.getReferenceById(outcome.criterionId()))
                            .tieBreakerScore(outcome.criterionScore());
                }
                builder.tieBreakerReason(outcome.reason());
            } else {
                builder.tieBreakerReason("Ranked by total weighted score");
            }
            RoundRanking ranking = rankingRepository.save(builder.build());
            saved.add(ranking);

            // Ghi mốc vào hành trình của đội: tính lại xếp hạng / thăng hạng / bị loại
            recordTimeline(team, round, ranking, applyPromotion);

            prevTotal = nz(row.getTotalScore());
            prevTeamId = row.getTeamId();
            rank++;
        }
        return saved.stream().map(RoundRankingMapper::toResponse).toList();
    }

    /**
     * EC tạo quyết định phân định hòa thủ công sau khi review mã nguồn GitHub.
     * Quyết định sẽ được áp dụng ở lần tính lại xếp hạng tiếp theo.
     */
    @Transactional
    public TieBreakDecisionResponse createTieBreakDecision(UUID roundId, CreateTieBreakDecisionRequest req, Authentication auth) {
        Round round = roundRepository.findById(roundId).orElseThrow(() -> ApiException.notFound("Round not found: " + roundId));
        Team team = teamRepository.findWithTrackById(req.teamId()).orElseThrow(() -> ApiException.notFound("Team not found: " + req.teamId()));
        // Đội phải thuộc đúng track của vòng thi
        if (!team.getTrack().getId().equals(round.getTrack().getId())) {
            throw ApiException.badRequest("Team does not belong to this round's track");
        }
        if (tieBreakDecisionRepository.existsByRoundIdAndTeamId(roundId, req.teamId())) {
            throw ApiException.conflict("A tie-break decision already exists for this team in this round");
        }
        UUID deciderId = currentUserId(auth);
        User decider = userRepository.findById(deciderId).orElseThrow(() -> ApiException.notFound("User not found: " + deciderId));

        TieBreakDecision decision = tieBreakDecisionRepository.save(TieBreakDecision.builder()
                .round(round)
                .team(team)
                .decidedBy(decider)
                .reason(req.reason().trim())
                .evidenceUrl(trimOrNull(req.evidenceUrl()))
                .note(trimOrNull(req.note()))
                .build());

        // Ghi mốc timeline: có quyết định phân định hòa thủ công cho đội này
        timelineService.record(team, round, TimelineEventType.TIE_BREAK_DECISION,
                "Manual tie-break decision",
                "Manual tie-break decision (GitHub source review) for round '" + round.getName() + "': " + decision.getReason()
                        + (decision.getEvidenceUrl() == null ? "" : " (evidence: " + decision.getEvidenceUrl() + ")"));
        return toDecisionResponse(decision);
    }

    /** Danh sách quyết định phân định hòa thủ công của một vòng. */
    @Transactional(readOnly = true)
    public List<TieBreakDecisionResponse> listTieBreakDecisions(UUID roundId) {
        return tieBreakDecisionRepository.findByRoundId(roundId).stream().map(this::toDecisionResponse).toList();
    }

    // ==== Helpers ====

    /** Chặn thăng hạng khi cửa sổ khiếu nại 15 phút còn mở. */
    private void ensureNoOpenAppealWindow(Round round) {
        if (round.getAppealDeadline() != null && LocalDateTime.now().isBefore(round.getAppealDeadline())) {
            throw ApiException.badRequest("Cannot apply promotion while the appeal window is still open (deadline: "
                    + round.getAppealDeadline() + ")");
        }
    }

    /** Chặn thăng hạng khi còn khiếu nại chưa được xử lý. */
    private void ensureNoUnresolvedAppeals(UUID roundId) {
        if (appealRepository.existsByRoundIdAndStatus(roundId, AppealStatus.PENDING)) {
            throw ApiException.badRequest("Cannot apply promotion while there are unresolved appeals for this round");
        }
    }

    /** Ghi mốc timeline sau khi lưu một dòng xếp hạng. */
    private void recordTimeline(Team team, Round round, RoundRanking ranking, boolean applyPromotion) {
        TimelineEventType type;
        String title;
        if (applyPromotion && ranking.getStatus() == PromotionStatus.promoted) {
            type = TimelineEventType.TEAM_PROMOTED;
            title = "Promoted to the next round";
        } else if (applyPromotion && ranking.getStatus() == PromotionStatus.eliminated) {
            type = TimelineEventType.TEAM_ELIMINATED;
            title = "Eliminated after ranking";
        } else {
            type = TimelineEventType.RANKING_RECALCULATED;
            title = "Ranking recalculated";
        }
        String description = "Round '" + round.getName() + "': rank " + ranking.getRank()
                + ", total score " + (ranking.getTotalScore() == null ? "0" : ranking.getTotalScore())
                + (ranking.getTieBreakerReason() == null ? "" : ". " + ranking.getTieBreakerReason());
        timelineService.record(team, round, type, title, description,
                ranking.getTotalScore(), ranking.getRank(), ranking.getStatus().name());
    }

    private TieBreakDecisionResponse toDecisionResponse(TieBreakDecision d) {
        return TieBreakDecisionResponse.builder()
                .id(d.getId())
                .roundId(d.getRound().getId())
                .teamId(d.getTeam().getId())
                .teamName(d.getTeam().getName())
                .decidedById(d.getDecidedBy().getId())
                .decidedByName(d.getDecidedBy().getFullName())
                .decidedAt(d.getDecidedAt())
                .reason(d.getReason())
                .evidenceUrl(d.getEvidenceUrl())
                .note(d.getNote())
                .build();
    }

    private UUID currentUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof CurrentUser c) return c.getId();
        throw ApiException.forbidden("Authentication required");
    }

    private static String trimOrNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    /**
     * Bộ phân định hòa: gom điểm theo tiêu chí (đã sắp theo trọng số giảm dần),
     * quyết định thủ công, số thành viên và thời điểm nộp bài của từng đội.
     * Cung cấp các comparator cho chuỗi sắp xếp và explain(...) để sinh lý do.
     */
    static final class TieBreaker {
        /** Danh sách tiêu chí (distinct) theo trọng số giảm dần. */
        private final List<CriterionRef> criteriaByWeightDesc = new ArrayList<>();
        /** teamId -> (criterionId -> điểm có trọng số trên tiêu chí đó). */
        private final Map<UUID, Map<UUID, BigDecimal>> byTeam = new HashMap<>();
        /** teamId -> quyết định phân định hòa thủ công (nếu có). */
        private final Map<UUID, TieBreakDecision> manualDecisions = new HashMap<>();
        /** teamId -> số thành viên. */
        private final Map<UUID, Long> memberCounts = new HashMap<>();
        /** teamId -> thời điểm nộp bài. */
        private final Map<UUID, LocalDateTime> submissionTimes = new HashMap<>();

        TieBreaker(List<RoundRankingRepository.TeamCriterionScoreRow> rows,
                   List<TieBreakDecision> decisions,
                   List<RoundRankingRepository.TeamMemberCountRow> counts,
                   List<RoundRankingRepository.TeamSubmissionTimeRow> times) {
            Set<UUID> seen = new HashSet<>();
            for (RoundRankingRepository.TeamCriterionScoreRow row : rows) {
                // rows đã sắp theo trọng số giảm dần -> lần đầu gặp một tiêu chí là đúng thứ tự ưu tiên
                if (seen.add(row.getCriterionId())) {
                    criteriaByWeightDesc.add(new CriterionRef(row.getCriterionId(), row.getCriterionName(), nz(row.getCriterionWeight())));
                }
                byTeam.computeIfAbsent(row.getTeamId(), k -> new HashMap<>())
                        .put(row.getCriterionId(), nz(row.getCriterionScore()));
            }
            for (TieBreakDecision d : decisions) manualDecisions.put(d.getTeam().getId(), d);
            for (RoundRankingRepository.TeamMemberCountRow c : counts) memberCounts.put(c.getTeamId(), c.getMemberCount());
            for (RoundRankingRepository.TeamSubmissionTimeRow t : times) submissionTimes.put(t.getTeamId(), t.getSubmittedAt());
        }

        /** Luật 1: đội có quyết định thủ công (review GitHub) xếp trước. */
        int compareByManualDecision(RoundRankingRepository.RoundScoreRow a, RoundRankingRepository.RoundScoreRow b) {
            boolean da = manualDecisions.containsKey(a.getTeamId());
            boolean db = manualDecisions.containsKey(b.getTeamId());
            return Boolean.compare(db, da); // true (có quyết định) đứng trước
        }

        /** Luật 2: điểm cao hơn trên tiêu chí trọng số cao nhất xếp trước. */
        int compareByCriterion(RoundRankingRepository.RoundScoreRow a, RoundRankingRepository.RoundScoreRow b) {
            for (CriterionRef c : criteriaByWeightDesc) {
                BigDecimal sa = scoreOf(a.getTeamId(), c.id());
                BigDecimal sb = scoreOf(b.getTeamId(), c.id());
                int cmp = sb.compareTo(sa); // giảm dần
                if (cmp != 0) return cmp;
            }
            return 0;
        }

        /** Luật 3: đội ÍT thành viên hơn xếp trước (hiệu suất trên đầu người cao hơn). */
        int compareByMemberCount(RoundRankingRepository.RoundScoreRow a, RoundRankingRepository.RoundScoreRow b) {
            long ca = memberCounts.getOrDefault(a.getTeamId(), Long.MAX_VALUE);
            long cb = memberCounts.getOrDefault(b.getTeamId(), Long.MAX_VALUE);
            return Long.compare(ca, cb); // tăng dần
        }

        /** Luật 4: đội nộp bài SỚM hơn xếp trước. */
        int compareBySubmissionTime(RoundRankingRepository.RoundScoreRow a, RoundRankingRepository.RoundScoreRow b) {
            LocalDateTime ta = submissionTimes.get(a.getTeamId());
            LocalDateTime tb = submissionTimes.get(b.getTeamId());
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return ta.compareTo(tb); // sớm hơn đứng trước
        }

        /**
         * Sinh lời giải thích cho đội xếp DƯỚI trong một cặp hòa tổng điểm:
         * đi lần lượt qua các luật, luật nào phân định được thì trả về lý do đó.
         */
        Outcome explain(UUID higherTeamId, UUID lowerTeamId) {
            // Luật 1: quyết định thủ công
            boolean higherManual = manualDecisions.containsKey(higherTeamId);
            boolean lowerManual = manualDecisions.containsKey(lowerTeamId);
            if (higherManual != lowerManual) {
                TieBreakDecision d = manualDecisions.get(higherManual ? higherTeamId : lowerTeamId);
                return new Outcome(null, null,
                        "Tie on total score broken by manual decision (GitHub source review): " + d.getReason());
            }
            // Luật 2: tiêu chí trọng số cao nhất
            for (CriterionRef c : criteriaByWeightDesc) {
                BigDecimal sh = scoreOf(higherTeamId, c.id());
                BigDecimal sl = scoreOf(lowerTeamId, c.id());
                if (sh.compareTo(sl) != 0) {
                    return new Outcome(c.id(), sl,
                            "Tie on total score broken by highest-weight criterion '" + c.name() + "'");
                }
            }
            // Luật 3: số thành viên
            Long ch = memberCounts.get(higherTeamId);
            Long cl = memberCounts.get(lowerTeamId);
            if (ch != null && cl != null && !ch.equals(cl)) {
                return new Outcome(null, null,
                        "Tie on total score and criteria broken by smaller team size (" + ch + " vs " + cl + " members)");
            }
            // Luật 4: thời điểm nộp bài
            LocalDateTime th = submissionTimes.get(higherTeamId);
            LocalDateTime tl = submissionTimes.get(lowerTeamId);
            if (th != null && tl != null && !th.equals(tl)) {
                return new Outcome(null, null,
                        "Tie on total score and criteria broken by earlier submission time");
            }
            // Không luật nào phân định được -> chỉ còn thứ tự tên đội (deterministic)
            return new Outcome(null, null,
                    "Fully tied; team name used for deterministic ordering. Consider a manual tie-break decision (GitHub source review)");
        }

        private BigDecimal scoreOf(UUID teamId, UUID criterionId) {
            Map<UUID, BigDecimal> m = byTeam.get(teamId);
            if (m == null) return BigDecimal.ZERO;
            return m.getOrDefault(criterionId, BigDecimal.ZERO);
        }

        record CriterionRef(UUID id, String name, BigDecimal weight) {}
        /** Kết quả giải thích: tiêu chí quyết định (nếu có) + điểm + câu lý do. */
        record Outcome(UUID criterionId, BigDecimal criterionScore, String reason) {}
    }
}
