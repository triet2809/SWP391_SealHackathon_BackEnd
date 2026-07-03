package vn.edu.fpt.seal.modules.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.seal.common.enums.EventStatus;
import vn.edu.fpt.seal.modules.event.dto.ChangeEventStatusRequest;
import vn.edu.fpt.seal.modules.event.dto.CreateEventRequest;
import vn.edu.fpt.seal.modules.event.dto.EventResponse;
import vn.edu.fpt.seal.modules.event.dto.UpdateEventRequest;
import vn.edu.fpt.seal.modules.event.service.EventService;

import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Hackathon event management")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List events (optional filter by status)")
    public ResponseEntity<Page<EventResponse>> list(
            @RequestParam(required = false) EventStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.list(status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get event by id")
    public ResponseEntity<EventResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Create new event (coordinator only)")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest req) {
        return ResponseEntity.ok(eventService.create(req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Update event metadata (coordinator only)")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody UpdateEventRequest req) {
        return ResponseEntity.ok(eventService.update(id, req));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Change event status (coordinator only)")
    public ResponseEntity<EventResponse> changeStatus(@PathVariable UUID id,
                                                      @Valid @RequestBody ChangeEventStatusRequest req) {
        return ResponseEntity.ok(eventService.changeStatus(id, req.status()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Delete event (only when status=draft)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
