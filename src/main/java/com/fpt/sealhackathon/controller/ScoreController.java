package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.ScoreRequest;
import com.fpt.sealhackathon.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ScoreController {
    @Autowired
    private ScoreService scoreService;

    @PostMapping("/submissions/{submissionId}/scores")
    public ResponseEntity<?> createScore(@PathVariable UUID submissionId, @RequestBody ScoreRequest request) {
        return ResponseEntity.ok(scoreService.createScore(submissionId, request));
    }
    @PutMapping("/scores/{scoreId}")
    public ResponseEntity<?> updateScore(
            @PathVariable UUID scoreId,
            @RequestBody ScoreRequest request) {

        return ResponseEntity.ok(
                scoreService.updateScore(scoreId, request)
        );
    }
    @DeleteMapping("/scores/{scoreId}")
    public ResponseEntity<?> deleteScore(
            @PathVariable UUID scoreId) {

        scoreService.deleteScore(scoreId);

        return ResponseEntity.ok("Deleted");
    }
    @GetMapping("/submissions/{submissionId}/scores")
    public ResponseEntity<?> getScoresBySubmission(
            @PathVariable UUID submissionId) {

        return ResponseEntity.ok(
                scoreService.getScoresBySubmission(submissionId)
        );

    }
    @GetMapping("/submissions/{submissionId}/score-summary")
    public ResponseEntity<?> getSummary(
            @PathVariable UUID submissionId) {

        return ResponseEntity.ok(
                scoreService.getScoreSummary(submissionId)
        );

    }
}
