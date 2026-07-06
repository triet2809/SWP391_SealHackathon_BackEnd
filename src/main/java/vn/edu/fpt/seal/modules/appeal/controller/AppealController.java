package vn.edu.fpt.seal.modules.appeal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.seal.common.enums.AppealStatus;
import vn.edu.fpt.seal.modules.appeal.dto.*;
import vn.edu.fpt.seal.modules.appeal.service.AppealService;
import vn.edu.fpt.seal.modules.round.dto.RoundResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appeals")
@RequiredArgsConstructor
@Tag(name = "Appeals", description = "Round result appeals with a 15-minute submission window")
public class AppealController {

    private final AppealService service;

    /** EC công bố kết quả vòng và mở cửa sổ khiếu nại 15 phút. */
    @PostMapping("/rounds/{roundId}/publish-results")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Publish round results and open the 15-minute appeal window (coordinator only)")
    public ResponseEntity<RoundResponse> publishResults(@PathVariable UUID roundId) {
        return ResponseEntity.ok(service.publishResults(roundId));
    }

    /** Thí sinh (leader/member) nộp khiếu nại — backend từ chối nếu quá hạn. */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit an appeal (team leader/member, within the appeal window only)")
    public ResponseEntity<AppealResponse> create(@Valid @RequestBody CreateAppealRequest req, Authentication auth) {
        return ResponseEntity.ok(service.create(req, auth));
    }

    /** EC duyệt danh sách khiếu nại theo sự kiện / vòng / trạng thái. */
    @GetMapping
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "List appeals (coordinator only)")
    public ResponseEntity<Page<AppealResponse>> search(@RequestParam(required = false) UUID eventId,
                                                       @RequestParam(required = false) UUID roundId,
                                                       @RequestParam(required = false) AppealStatus status,
                                                       Pageable pageable) {
        return ResponseEntity.ok(service.search(eventId, roundId, status, pageable));
    }

    /** Đội xem lại các khiếu nại của mình (EC xem được mọi đội). */
    @GetMapping("/teams/{teamId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List appeals of a team (coordinator, or a member of that team)")
    public ResponseEntity<List<AppealResponse>> byTeam(@PathVariable UUID teamId, Authentication auth) {
        return ResponseEntity.ok(service.byTeam(teamId, auth));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Get appeal by id (coordinator only)")
    public ResponseEntity<AppealResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    /** EC ghi phản hồi trung gian, đơn vẫn PENDING. */
    @PostMapping("/{id}/respond")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Add a response to a pending appeal (coordinator only)")
    public ResponseEntity<AppealResponse> respond(@PathVariable UUID id, @Valid @RequestBody RespondAppealRequest req) {
        return ResponseEntity.ok(service.respond(id, req));
    }

    /** EC chốt kết luận ACCEPTED / REJECTED. */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Resolve an appeal as ACCEPTED or REJECTED (coordinator only)")
    public ResponseEntity<AppealResponse> resolve(@PathVariable UUID id, @Valid @RequestBody ResolveAppealRequest req,
                                                  Authentication auth) {
        return ResponseEntity.ok(service.resolve(id, req, auth));
    }
}
