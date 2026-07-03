package vn.edu.fpt.seal.modules.judge.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.modules.judge.entity.RoundJudge;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface RoundJudgeRepository extends JpaRepository<RoundJudge, UUID> {
    @EntityGraph(attributePaths = {"round", "user"}) Page<RoundJudge> findByRoundId(UUID roundId, Pageable pageable);
    @EntityGraph(attributePaths = {"round", "user"}) Page<RoundJudge> findByUserId(UUID userId, Pageable pageable);
    @EntityGraph(attributePaths = {"round", "user"}) Optional<RoundJudge> findWithRelationsById(UUID id);
    boolean existsByRoundIdAndUserId(UUID roundId, UUID userId);
    Optional<RoundJudge> findByRoundIdAndUserId(UUID roundId, UUID userId);

    @Query(value = """
            select rj.id as roundJudgeId, rj.user_id as judgeId, r.id as roundId, r.name as roundName,
                   t.id as teamId, t.name as teamName, s.id as submissionId, s.submitted_at as submittedAt
            from round_judges rj
            join rounds r on r.id = rj.round_id
            join submissions s on s.round_id = r.id
            join teams t on t.id = s.team_id
            where rj.user_id = :judgeId
            order by r.submission_deadline asc, t.name asc
            """, nativeQuery = true)
    List<JudgeSubmissionRow> findSubmissionRowsForJudge(@Param("judgeId") UUID judgeId);

    interface JudgeSubmissionRow {
        UUID getRoundJudgeId(); UUID getJudgeId(); UUID getRoundId(); String getRoundName(); UUID getTeamId(); String getTeamName(); UUID getSubmissionId(); LocalDateTime getSubmittedAt();
    }
}
