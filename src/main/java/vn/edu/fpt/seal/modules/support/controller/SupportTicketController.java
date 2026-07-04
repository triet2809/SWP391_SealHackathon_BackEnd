package vn.edu.fpt.seal.modules.support.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.seal.modules.support.dto.*;
import vn.edu.fpt.seal.modules.support.service.SupportTicketService;
import vn.edu.fpt.seal.security.CurrentUser;

import java.util.*;

@RestController
@RequestMapping("/support-tickets")
@RequiredArgsConstructor
public class SupportTicketController {
    private final SupportTicketService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SupportTicketResponse>> list(@AuthenticationPrincipal CurrentUser user,
                                                            @RequestParam(required = false) UUID requesterId) {
        return ResponseEntity.ok(service.list(user, requesterId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupportTicketResponse> create(@AuthenticationPrincipal CurrentUser user,
                                                        @Valid @RequestBody CreateSupportTicketRequest r) {
        return ResponseEntity.ok(service.create(r, user.getId()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<SupportTicketResponse> updateStatus(@AuthenticationPrincipal CurrentUser user,
                                                              @PathVariable UUID id,
                                                              @Valid @RequestBody UpdateSupportTicketStatusRequest r) {
        return ResponseEntity.ok(service.updateStatus(user, id, r));
    }
}
