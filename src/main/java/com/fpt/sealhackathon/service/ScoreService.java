package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.ScoreRequest;
import com.fpt.sealhackathon.dto.ScoreSummaryResponse;
import com.fpt.sealhackathon.entity.Score;
import com.fpt.sealhackathon.entity.Submission;
import com.fpt.sealhackathon.repository.RoundJudgeRepository;
import com.fpt.sealhackathon.repository.ScoreRepository;
import com.fpt.sealhackathon.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service

public class ScoreService {
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private RoundJudgeRepository roundJudgeRepository;

    public Score createScore(UUID submissionId, ScoreRequest request) {
        Submission submission = submissionRepository.findById(submissionId).orElseThrow(() -> new RuntimeException("Submission not found"));
        UUID judgeId = request.getJudgeId();
        if (!roundJudgeRepository.existsByRoundTrackIdAndUserId(submission.getRoundTrackId(), judgeId)) {

            throw new RuntimeException("Judge is not assigned to this submission");
        }
        if (scoreRepository.existsBySubmissionIdAndJudgeIdAndCriterionId(submissionId, judgeId, request.getCriterionId())) {

            throw new RuntimeException("Score already exists");
        }
        Score score = new Score();

        score.setId(UUID.randomUUID());

        score.setSubmissionId(submissionId);

        score.setJudgeId(judgeId);

        score.setCriterionId(request.getCriterionId());

        score.setScore(request.getScore());

        score.setWeightedScore(request.getScore());

        score.setComment(request.getComment());

        score.setCreatedAt(LocalDateTime.now());

        return scoreRepository.save(score);
    }
    public Score updateScore(UUID scoreId, ScoreRequest request) {

        Score score = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Score not found"));

        score.setScore(request.getScore());

        score.setWeightedScore(request.getScore());

        score.setComment(request.getComment());

        score.setUpdatedAt(LocalDateTime.now());

        return scoreRepository.save(score);
    }
    public void deleteScore(UUID scoreId) {

        Score score = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Score not found"));

        scoreRepository.delete(score);
    }
    public List<Score> getScoresBySubmission(UUID submissionId) {

        return scoreRepository.findBySubmissionId(submissionId);

    }
    public ScoreSummaryResponse getScoreSummary(UUID submissionId) {
        List<Score> scores = scoreRepository.findBySubmissionId(submissionId);
        if (scores.isEmpty()) {
            throw new RuntimeException("No scores found");
        }
        BigDecimal total = BigDecimal.ZERO;

        for (Score score : scores) {
            total = total.add(score.getScore());
        }
        BigDecimal average = total.divide(
                BigDecimal.valueOf(scores.size()),
                2,
                RoundingMode.HALF_UP
        );
        BigDecimal weightedTotal = BigDecimal.ZERO;

        for (Score score : scores) {
            weightedTotal = weightedTotal.add(score.getWeightedScore());
        }

        BigDecimal weightedAverage = weightedTotal.divide(
                BigDecimal.valueOf(scores.size()),
                2,
                RoundingMode.HALF_UP
        );
        BigDecimal varianceTotal = BigDecimal.ZERO;

        for (Score score : scores) {

            BigDecimal diff = score.getScore().subtract(average);

            BigDecimal squared = diff.multiply(diff);

            varianceTotal = varianceTotal.add(squared);
        }

        BigDecimal variance = varianceTotal.divide(
                BigDecimal.valueOf(scores.size()),
                4,
                RoundingMode.HALF_UP
        );
        double std = Math.sqrt(variance.doubleValue());
        BigDecimal stdDev = BigDecimal.valueOf(std)
                .setScale(4, RoundingMode.HALF_UP);
        ScoreSummaryResponse response = new ScoreSummaryResponse();

        response.setAverageScore(average);

        response.setWeightedScore(weightedAverage);
        response.setVariance(variance);
        response.setStandardDeviation(stdDev);
        return response;

    }
}
