package vn.edu.fpt.seal.modules.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.seal.modules.notification.dto.NotificationResponse;
import vn.edu.fpt.seal.modules.notification.service.NotificationService;
import vn.edu.fpt.seal.security.CurrentUser;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal CurrentUser user,
                                                           @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(service.list(user.getId(), unreadOnly));
    }

    /** Unread total + per-category counts (drives the red dots). */
    @GetMapping("/unread-summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> unreadSummary(@AuthenticationPrincipal CurrentUser user) {
        return ResponseEntity.ok(service.unreadSummary(user.getId()));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
        service.markRead(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal CurrentUser user,
                                            @RequestParam(required = false) String category) {
        service.markAllRead(user.getId(), category);
        return ResponseEntity.noContent().build();
    }
}
