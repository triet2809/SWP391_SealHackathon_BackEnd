package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.roundcriteria.RoundCriteriaRequest;
import com.fpt.sealhackathon.dto.roundcriteria.RoundCriteriaResponse;
import com.fpt.sealhackathon.service.RoundCriteriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/round-criteria")
@RequiredArgsConstructor
public class RoundCriteriaController {

    private final RoundCriteriaService roundCriteriaService;

    @PostMapping
    public ResponseEntity<RoundCriteriaResponse> createRoundCriteria(
            @Valid @RequestBody RoundCriteriaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roundCriteriaService.createRoundCriteria(request));
    }

    @GetMapping
    public ResponseEntity<List<RoundCriteriaResponse>> getAllRoundCriteria() {
        return ResponseEntity.ok(roundCriteriaService.getAllRoundCriteria());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoundCriteriaResponse> getRoundCriteriaById(@PathVariable UUID id) {
        return ResponseEntity.ok(roundCriteriaService.getRoundCriteriaById(id));
    }

    @GetMapping("/round/{roundId}")
    public ResponseEntity<List<RoundCriteriaResponse>> getRoundCriteriaByRoundId(@PathVariable UUID roundId) {
        return ResponseEntity.ok(roundCriteriaService.getRoundCriteriaByRoundId(roundId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoundCriteriaResponse> updateRoundCriteria(
            @PathVariable UUID id,
            @Valid @RequestBody RoundCriteriaRequest request
    ) {
        return ResponseEntity.ok(roundCriteriaService.updateRoundCriteria(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoundCriteria(@PathVariable UUID id) {
        roundCriteriaService.deleteRoundCriteria(id);
        return ResponseEntity.noContent().build();
    }
}
