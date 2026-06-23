package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.roundtrack.RoundTrackRequest;
import com.fpt.sealhackathon.dto.roundtrack.RoundTrackResponse;
import com.fpt.sealhackathon.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/round-tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @PostMapping
    public ResponseEntity<RoundTrackResponse> createTrack(@Valid @RequestBody RoundTrackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trackService.createTrack(request));
    }

    @GetMapping
    public ResponseEntity<List<RoundTrackResponse>> getAllTracks() {
        return ResponseEntity.ok(trackService.getAllTracks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoundTrackResponse> getTrackById(@PathVariable UUID id) {
        return ResponseEntity.ok(trackService.getTrackById(id));
    }

    @GetMapping("/round/{roundId}")
    public ResponseEntity<List<RoundTrackResponse>> getTracksByRoundId(@PathVariable UUID roundId) {
        return ResponseEntity.ok(trackService.getTracksByRoundId(roundId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoundTrackResponse> updateTrack(
            @PathVariable UUID id,
            @Valid @RequestBody RoundTrackRequest request
    ) {
        return ResponseEntity.ok(trackService.updateTrack(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(@PathVariable UUID id) {
        trackService.deleteTrack(id);
        return ResponseEntity.noContent().build();
    }
}
