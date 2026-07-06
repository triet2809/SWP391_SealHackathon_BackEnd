package vn.edu.fpt.seal.modules.teamtimeline.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.seal.modules.teamtimeline.dto.TeamTimelineEventResponse;
import vn.edu.fpt.seal.modules.teamtimeline.service.TeamTimelineService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/team-timeline")
@RequiredArgsConstructor
@Tag(name = "Team Timeline", description = "Team journey timeline (creation, promotions, appeals, prizes...)")
public class TeamTimelineController {

    private final TeamTimelineService service;

    /** Thí sinh xem hành trình của (các) đội mình đang tham gia. */
    @GetMapping("/my-team")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Timeline for the current user's team(s)")
    public ResponseEntity<List<TeamTimelineEventResponse>> myTeam(
            @RequestParam(required = false) UUID eventId, Authentication auth) {
        return ResponseEntity.ok(service.myTeamTimeline(auth, eventId));
    }

    /** Xem timeline của một đội cụ thể (EC xem mọi đội, thí sinh chỉ xem đội mình). */
    @GetMapping("/teams/{teamId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Timeline for a specific team (coordinator, or a member of that team)")
    public ResponseEntity<List<TeamTimelineEventResponse>> byTeam(@PathVariable UUID teamId, Authentication auth) {
        return ResponseEntity.ok(service.teamTimeline(teamId, auth));
    }

    /** EC xem toàn bộ mốc của mọi đội trong một sự kiện (phân trang). */
    @GetMapping
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Timeline for all teams in an event (coordinator only)")
    public ResponseEntity<Page<TeamTimelineEventResponse>> byEvent(@RequestParam UUID eventId, Pageable pageable) {
        return ResponseEntity.ok(service.eventTimeline(eventId, pageable));
    }
}
