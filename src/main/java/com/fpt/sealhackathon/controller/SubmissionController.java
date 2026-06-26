package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.SubmissionRequest;
import com.fpt.sealhackathon.entity.Submission;
import com.fpt.sealhackathon.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubmission(
            @PathVariable UUID id) {

        Submission submission =
                submissionService.getSubmission(id);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submission);
    }
    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getTeamSubmissions(
            @PathVariable UUID teamId){

        return ResponseEntity.ok(
                submissionService.getTeamSubmissions(teamId)
        );
    }
    @GetMapping("/round/{roundId}")
    public ResponseEntity<?> getRoundSubmissions(@PathVariable UUID roundId){

        return ResponseEntity.ok(
                submissionService.getRoundSubmissions(roundId)
        );
    }
    @PostMapping("/round-participants/{participantId}")
    public ResponseEntity<?> createSubmission(
            @PathVariable UUID participantId) {

        Submission submission =
                submissionService.createSubmission(participantId);

        if (submission == null) {
            return ResponseEntity.badRequest()
                    .body("Submission already exists");
        }

        return ResponseEntity.ok(submission);
    }
    @PutMapping("/{submissionId}")
    public ResponseEntity<?> updateSubmission(
            @PathVariable UUID submissionId,
            @RequestBody SubmissionRequest request) {

        Submission submission =
                submissionService.updateSubmission(submissionId, request);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submission);
    }
    @PatchMapping("/{submissionId}/submit")
    public ResponseEntity<?> submitSubmission(
            @PathVariable UUID submissionId) {

        Submission submission =
                submissionService.submitSubmission(submissionId);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submission);
    }
    @GetMapping("/round-participants/{participantId}")
    public ResponseEntity<?> getSubmissionByParticipant(
            @PathVariable UUID participantId) {

        Submission submission =
                submissionService.getSubmissionByParticipant(participantId);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submission);
    }
    @PatchMapping("/{submissionId}/accept")
    public ResponseEntity<?> acceptSubmission(
            @PathVariable UUID submissionId) {

        Submission submission =
                submissionService.acceptSubmission(submissionId);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submission);
    }
    @PatchMapping("/{submissionId}/late")
    public ResponseEntity<?> lateSubmission(
            @PathVariable UUID submissionId) {
        Submission submission =
                submissionService.lateSubmission(submissionId);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submission);
    }
    @PatchMapping("/{submissionId}/invalid")
    public ResponseEntity<?> invalidSubmission(
            @PathVariable UUID submissionId) {
        Submission submission =
                submissionService.invalidSubmission(submissionId);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submission);
    }
}
