package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.round.RoundRequest;
import com.fpt.sealhackathon.dto.round.RoundResponse;
import com.fpt.sealhackathon.service.RoundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final RoundService roundService;

    @PostMapping
    public ResponseEntity<RoundResponse> createRound(@Valid @RequestBody RoundRequest request) {
        RoundResponse response = roundService.createRound(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoundResponse>> getAllRounds() {
        return ResponseEntity.ok(roundService.getAllRounds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoundResponse> getRoundById(@PathVariable UUID id) {
        return ResponseEntity.ok(roundService.getRoundById(id));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<RoundResponse>> getRoundsByEventId(@PathVariable UUID eventId) {
        return ResponseEntity.ok(roundService.getRoundsByEventId(eventId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoundResponse> updateRound(
            @PathVariable UUID id,
            @Valid @RequestBody RoundRequest request
    ) {
        return ResponseEntity.ok(roundService.updateRound(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRound(@PathVariable UUID id) {
        roundService.deleteRound(id);
        return ResponseEntity.noContent().build();
    }
}
