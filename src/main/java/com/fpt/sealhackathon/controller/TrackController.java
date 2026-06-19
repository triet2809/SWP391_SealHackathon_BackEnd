package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.track.TrackRequest;
import com.fpt.sealhackathon.dto.track.TrackResponse;
import com.fpt.sealhackathon.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @PostMapping
    public ResponseEntity<TrackResponse> createTrack(@Valid @RequestBody TrackRequest request) {
        TrackResponse response = trackService.createTrack(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TrackResponse>> getAllTracks() {
        return ResponseEntity.ok(trackService.getAllTracks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackResponse> getTrackById(@PathVariable UUID id) {
        return ResponseEntity.ok(trackService.getTrackById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrackResponse> updateTrack(
            @PathVariable UUID id,
            @Valid @RequestBody TrackRequest request
    ) {
        return ResponseEntity.ok(trackService.updateTrack(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(@PathVariable UUID id) {
        trackService.deleteTrack(id);
        return ResponseEntity.noContent().build();
    }
}
