package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.MentorFeedbackRequest;
import com.fpt.sealhackathon.service.MentorFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/teams")
public class MentorFeedbackController {
    @Autowired
    private MentorFeedbackService mentorFeedbackService;

    @PostMapping("/teams/{teamId}/mentor-feedbacks")
    public ResponseEntity<?> createFeedback(
            @PathVariable UUID teamId,
            @RequestBody MentorFeedbackRequest request) {

        return ResponseEntity.ok(
                mentorFeedbackService.createFeedback(teamId, request));
    }
}
