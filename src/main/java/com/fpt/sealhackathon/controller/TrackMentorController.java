package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.JudgeAssignRequest;
import com.fpt.sealhackathon.dto.MentorAssignRequest;
import com.fpt.sealhackathon.service.TrackMentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/round-tracks")
public class TrackMentorController {
    @Autowired
    private TrackMentorService trackMentorService;
    @GetMapping("/{roundTrackId}/mentors")
    public ResponseEntity<?> getMentorsByRoundTrack(@PathVariable UUID roundTrackId) {
        return ResponseEntity.ok(trackMentorService.getMentorsByRoundTrack(roundTrackId));
    }
    @PostMapping("/{roundId}/mentors")
    public ResponseEntity<?> assignMentor(@PathVariable UUID roundId, @RequestBody MentorAssignRequest request) {

        return ResponseEntity.ok(trackMentorService.assignMentor(roundId, request)
        );
    }

    @DeleteMapping("/{mentorId}")
    public ResponseEntity<?> removeJudge(@PathVariable UUID mentorId) {

        boolean deleted = trackMentorService.removeMentor(mentorId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("Mentor removed successfully");
    }


    @GetMapping("/me/teams")
    public ResponseEntity<?> getMyTeams() {

        // Tạm hardcode để test
        UUID mentorId = UUID.fromString("mentor-id-trong-db");

        return ResponseEntity.ok(
                trackMentorService.getMyTeams(mentorId)
        );
    }

}
