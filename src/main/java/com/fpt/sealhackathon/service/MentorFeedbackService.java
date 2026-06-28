package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.MentorFeedbackRequest;
import com.fpt.sealhackathon.entity.MentorFeedback;
import com.fpt.sealhackathon.entity.RoundParticipant;
import com.fpt.sealhackathon.repository.MentorFeedbackRepository;
import com.fpt.sealhackathon.repository.RoundParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MentorFeedbackService {
    @Autowired
    private MentorFeedbackRepository mentorFeedbackRepository;
    @Autowired
    private RoundParticipantRepository roundParticipantRepository;

    public MentorFeedback createFeedback(UUID teamId, MentorFeedbackRequest request){
        RoundParticipant participant =
                roundParticipantRepository.findByTeamId(teamId).orElseThrow(() -> new IllegalArgumentException("Participant not found"));
        UUID mentorId = UUID.fromString("1");
        MentorFeedback feedback = new MentorFeedback();
        feedback.setId(UUID.randomUUID());

        feedback.setMentorId(mentorId);

        feedback.setEventId(participant.getEventId());

        feedback.setRoundId(participant.getRoundId());

        feedback.setRoundTrackId(participant.getRoundTrackId());

        feedback.setTeamId(teamId);

        feedback.setContent(request.getContent());

        feedback.setCreatedAt(LocalDateTime.now());

        return mentorFeedbackRepository.save(feedback);
    }
}
