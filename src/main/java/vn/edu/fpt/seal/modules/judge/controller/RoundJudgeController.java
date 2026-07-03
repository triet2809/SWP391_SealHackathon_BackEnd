package vn.edu.fpt.seal.modules.judge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.seal.modules.judge.dto.*;
import vn.edu.fpt.seal.modules.judge.service.RoundJudgeService;

import java.util.*;

@RestController @RequestMapping("/round-judges") @RequiredArgsConstructor @Tag(name = "Round Judges")
public class RoundJudgeController {
    private final RoundJudgeService service;
    @GetMapping @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<RoundJudgeResponse>> list(@RequestParam(required = false) UUID roundId, @RequestParam(required = false) UUID userId, Pageable pageable) { return ResponseEntity.ok(service.list(roundId, userId, pageable)); }
    @PostMapping @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<RoundJudgeResponse> assign(@Valid @RequestBody AssignRoundJudgeRequest req) { return ResponseEntity.ok(service.assign(req)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<Void> remove(@PathVariable UUID id) { service.remove(id); return ResponseEntity.noContent().build(); }
    @DeleteMapping @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<Void> removeByRoundAndUser(@RequestParam UUID roundId, @RequestParam UUID userId) { service.removeByRoundAndUser(roundId, userId); return ResponseEntity.noContent().build(); }
    @GetMapping("/judges/{judgeId}/submissions") @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<JudgeSubmissionResponse>> submissions(@PathVariable UUID judgeId) { return ResponseEntity.ok(service.submissions(judgeId)); }
}
