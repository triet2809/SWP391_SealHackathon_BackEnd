package vn.edu.fpt.seal.modules.rules.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.seal.modules.rules.dto.*;
import vn.edu.fpt.seal.modules.rules.service.EventRuleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/event-rules")
@RequiredArgsConstructor
@Tag(name = "Event Rules", description = "Event rules with visibility levels and rule acceptances")
public class EventRuleController {

    private final EventRuleService service;

    /** Thí sinh chỉ nhận rule PUBLIC; điều phối viên nhận tất cả (lọc ở service). */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List rules of an event (contestants only see PUBLIC rules)")
    public ResponseEntity<List<EventRuleResponse>> list(@RequestParam UUID eventId, Authentication auth) {
        return ResponseEntity.ok(service.list(eventId, auth));
    }

    @PostMapping
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Create an event rule (coordinator only)")
    public ResponseEntity<EventRuleResponse> create(@Valid @RequestBody CreateEventRuleRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Update an event rule (coordinator only)")
    public ResponseEntity<EventRuleResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateEventRuleRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Delete an event rule (coordinator only)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Checkbox "I agree to the event rules" trên UI gọi endpoint này. */
    @PostMapping("/acceptances")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accept the rules of an event (idempotent)")
    public ResponseEntity<RuleAcceptanceResponse> accept(@Valid @RequestBody AcceptRulesRequest req, Authentication auth) {
        return ResponseEntity.ok(service.accept(req, auth));
    }

    /** FE kiểm tra xem user đã chấp nhận thể lệ chưa để ẩn/hiện checkbox. */
    @GetMapping("/acceptances/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check whether the current user accepted the rules of an event")
    public ResponseEntity<RuleAcceptanceResponse> myAcceptance(@RequestParam UUID eventId, Authentication auth) {
        return ResponseEntity.ok(service.myAcceptance(eventId, auth));
    }
}
