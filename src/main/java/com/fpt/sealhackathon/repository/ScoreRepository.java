package com.fpt.sealhackathon.repository;

import com.fpt.sealhackathon.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ScoreRepository extends JpaRepository<Score, UUID> {
    List<Score> findBySubmissionId(UUID submissionId);

    List<Score> findByJudgeId(UUID judgeId);

    List<Score> findByCriterionId(UUID criterionId);

    Optional<Score> findBySubmissionIdAndJudgeIdAndCriterionId(
            UUID submissionId,
            UUID judgeId,
            UUID criterionId
    );

    boolean existsBySubmissionIdAndJudgeIdAndCriterionId(
            UUID submissionId,
            UUID judgeId,
            UUID criterionId
    );
}
