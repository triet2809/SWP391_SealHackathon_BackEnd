package com.fpt.sealhackathon.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.sealhackathon.dto.event.request.TrackRegisterRequest;
import com.fpt.sealhackathon.entity.Track;
import com.fpt.sealhackathon.service.TrackService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @PostMapping
    public ResponseEntity<Track> registerTrack(@Valid @RequestBody TrackRegisterRequest request) {
        Track response = trackService.registeTrack(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}