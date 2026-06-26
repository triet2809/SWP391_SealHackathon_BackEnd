package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.SubmissionRequest;
import com.fpt.sealhackathon.entity.RoundParticipant;
import com.fpt.sealhackathon.entity.Submission;
import com.fpt.sealhackathon.entity.SubmissionStatus;
import com.fpt.sealhackathon.repository.RoundParticipantRepository;
import com.fpt.sealhackathon.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
public class SubmissionService {
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private RoundParticipantRepository roundParticipantRepository;
    public Submission getSubmission(UUID id) {
        return submissionRepository.findById(id).orElse(null);
    }

    public List<Submission> getTeamSubmissions(UUID teamId) {
        return submissionRepository.findByTeamId(teamId);
    }
    public List<Submission> getRoundSubmissions(UUID roundId){
        return submissionRepository.findByRoundId(roundId);
    }
    public Submission getSubmissionByParticipant(UUID participantId) {
        return submissionRepository.findByRoundParticipantId(participantId);
    }
    public Submission createSubmission(UUID participantId) {

        if (submissionRepository.existsByRoundParticipantId(participantId)) {
            return null;
        }

        RoundParticipant rp = roundParticipantRepository
                .findById(participantId)
                .orElseThrow(() -> new RuntimeException("Round participant not found"));

        Submission submission = new Submission();
        submission.setId(UUID.randomUUID());
        submission.setRoundParticipantId(rp.getId());
        submission.setEventId(rp.getEventId());
        submission.setRoundId(rp.getRoundId());
        submission.setRoundTrackId(rp.getRoundTrackId());
        submission.setTeamId(rp.getTeamId());

        submission.setStatus(SubmissionStatus.draft);

        submission.setSubmittedAt(LocalDateTime.now());
        submission.setUpdatedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }
    public Submission updateSubmission(
            UUID submissionId,
            SubmissionRequest request) {

        Submission submission =
                submissionRepository.findById(submissionId)
                        .orElse(null);

        if (submission == null) {
            return null;
        }

        submission.setRepoUrl(request.getRepoUrl());
        submission.setDemoUrl(request.getDemoUrl());
        submission.setSlideUrl(request.getSlideUrl());
        submission.setReportUrl(request.getReportUrl());
        submission.setApiMetadata(request.getApiMetadata());

        submission.setUpdatedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }
    public Submission submitSubmission(UUID submissionId) {

        Submission submission =
                submissionRepository.findById(submissionId)
                        .orElse(null);

        if (submission == null) {
            return null;
        }
        if (submission.getStatus() != SubmissionStatus.draft) {
            throw new RuntimeException("Submission has already been submitted.");
        }

        submission.setStatus(SubmissionStatus.submitted);
        submission.setSubmittedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }
    public Submission acceptSubmission(UUID submissionId) {

        Submission submission = submissionRepository
                .findById(submissionId)
                .orElse(null);

        if (submission == null) {
            return null;
        }

        // Chỉ accept nếu đã submit
        if (submission.getStatus() != SubmissionStatus.submitted) {
            throw new RuntimeException("Submission must be SUBMITTED before accepting.");
        }

        submission.setStatus(SubmissionStatus.accepted);
        submission.setUpdatedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }
    public Submission lateSubmission(UUID submissionId) {

        Submission submission = submissionRepository
                .findById(submissionId)
                .orElse(null);

        if (submission == null) {
            return null;
        }

        // Chỉ reject nếu đã submit
        if (submission.getStatus() != SubmissionStatus.submitted) {
            throw new RuntimeException("Submission must be SUBMITTED before rejecting.");
        }

        submission.setStatus(SubmissionStatus.late);
        submission.setUpdatedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }
    public Submission invalidSubmission(UUID submissionId) {

        Submission submission = submissionRepository
                .findById(submissionId)
                .orElse(null);

        if (submission == null) {
            return null;
        }

        // Chỉ reject nếu đã submit
        if (submission.getStatus() != SubmissionStatus.invalid) {
            throw new RuntimeException("Submission must be SUBMITTED before rejecting.");
        }

        submission.setStatus(SubmissionStatus.late);
        submission.setUpdatedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }
}