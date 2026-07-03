package vn.edu.fpt.seal.modules.ranking.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.seal.modules.ranking.dto.*;
import vn.edu.fpt.seal.modules.ranking.service.RoundRankingService;

import java.util.*;

@RestController
@RequestMapping("/round-rankings")
@RequiredArgsConstructor
@Tag(name = "Round Rankings")
public class RoundRankingController {
    private final RoundRankingService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<RoundRankingResponse>> list(@RequestParam UUID roundId, Pageable pageable) {
        return ResponseEntity.ok(service.list(roundId, pageable));
    }

    @PostMapping("/rounds/{roundId}/recalculate")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<List<RoundRankingResponse>> recalculate(@PathVariable UUID roundId,
                                                                  @Valid @RequestBody(required = false) RecalculateRankingsRequest req) {
        return ResponseEntity.ok(service.recalculate(roundId, req));
    }
}
