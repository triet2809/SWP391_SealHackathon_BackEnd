package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.JudgeAssignRequest;
import com.fpt.sealhackathon.service.RoundJudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/rounds")
public class RoundJudgeController {
    @Autowired
    private RoundJudgeService roundJudgeService;

    @GetMapping("/{roundId}/judges")
    public ResponseEntity<?> getJudgesByRound(@PathVariable UUID roundId){

        return ResponseEntity.ok(roundJudgeService.getJudgesByRound(roundId)
        );
    }
    @PostMapping("/{roundId}/judges")
    public ResponseEntity<?> assignJudge(@PathVariable UUID roundId, @RequestBody JudgeAssignRequest request) {

        return ResponseEntity.ok(roundJudgeService.assignJudge(roundId, request)
        );
    }
    @GetMapping("/{roundTrackId}/judges")
    public ResponseEntity<?> getJudgesByTrack(@PathVariable UUID roundTrackId) {

        return ResponseEntity.ok(roundJudgeService.getJudgesByTrack(roundTrackId)
        );
    }
    @DeleteMapping("/{roundJudgeId}")
    public ResponseEntity<?> removeJudge(@PathVariable UUID roundJudgeId) {

        boolean deleted = roundJudgeService.removeJudge(roundJudgeId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("Judge removed successfully");
    }
}
