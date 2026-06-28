package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.enums.TeamMemberStatus;
import com.fpt.sealhackathon.dto.request.ChangeRoleRequest;
import com.fpt.sealhackathon.dto.request.CreateTeamRequest;
import com.fpt.sealhackathon.dto.request.InviteMemberRequest;
import com.fpt.sealhackathon.dto.request.TeamListRequest;
import com.fpt.sealhackathon.dto.request.UpdateTeamRequest;
import com.fpt.sealhackathon.dto.response.PagedResponse;
import com.fpt.sealhackathon.dto.response.TeamDetailResponse;
import com.fpt.sealhackathon.dto.response.TeamMemberResponse;
import com.fpt.sealhackathon.dto.response.TeamSummaryResponse;
import com.fpt.sealhackathon.service.TeamService;
import com.fpt.sealhackathon.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Team and Team Member management.
 * Thin layer — all validation and business logic lives in {@link TeamService}.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // -------------------------------------------------------------------------
    // GET /events/{eventId}/teams
    // -------------------------------------------------------------------------

    /**
     * Lists all teams for an event with optional filters.
     * Open to all authenticated users.
     */
    @GetMapping("/events/{eventId}/teams")
    public ResponseEntity<PagedResponse<TeamSummaryResponse>> listTeams(
            @PathVariable UUID eventId,
            @Valid @ModelAttribute TeamListRequest request) {

        PagedResponse<TeamSummaryResponse> response = teamService.listTeams(eventId, request);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // POST /events/{eventId}/teams
    // -------------------------------------------------------------------------

    /**
     * Creates a new team. Caller becomes team leader automatically.
     */
    @PostMapping("/events/{eventId}/teams")
    public ResponseEntity<TeamDetailResponse> createTeam(
            @PathVariable UUID eventId,
            @Valid @RequestBody CreateTeamRequest request) {

        UUID callerId = SecurityUtils.getCallerId();
        TeamDetailResponse response = teamService.createTeam(eventId, callerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------------------------------------------------------------
    // GET /teams/{teamId}
    // -------------------------------------------------------------------------

    /**
     * Returns full team detail including all members.
     */
    @GetMapping("/teams/{teamId}")
    public ResponseEntity<TeamDetailResponse> getTeam(
            @PathVariable UUID teamId) {

        return ResponseEntity.ok(teamService.getTeam(teamId));
    }

    // -------------------------------------------------------------------------
    // PUT /teams/{teamId}
    // -------------------------------------------------------------------------

    /**
     * Updates team name and/or description. Only the team leader may call this.
     */
    @PutMapping("/teams/{teamId}")
    public ResponseEntity<TeamDetailResponse> updateTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody UpdateTeamRequest request) {

        UUID callerId = SecurityUtils.getCallerId();
        TeamDetailResponse response = teamService.updateTeam(teamId, callerId, request);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // DELETE /teams/{teamId}
    // -------------------------------------------------------------------------

    /**
     * Deletes a team. Only permitted when team status is WAITING_FOR_MEMBERS.
     */
    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable UUID teamId) {

        UUID callerId = SecurityUtils.getCallerId();
        teamService.deleteTeam(teamId, callerId);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // GET /teams/{teamId}/members
    // -------------------------------------------------------------------------

    /**
     * Lists members of a team, optionally filtered by status.
     */
    @GetMapping("/teams/{teamId}/members")
    public ResponseEntity<List<TeamMemberResponse>> listMembers(
            @PathVariable UUID teamId,
            @RequestParam(required = false) TeamMemberStatus status) {

        return ResponseEntity.ok(teamService.listMembers(teamId, status));
    }

    // -------------------------------------------------------------------------
    // POST /teams/{teamId}/members/invite
    // -------------------------------------------------------------------------

    /**
     * Invites a user to the team. Only the team leader may send invitations.
     */
    @PostMapping("/teams/{teamId}/members/invite")
    public ResponseEntity<TeamMemberResponse> inviteMember(
            @PathVariable UUID teamId,
            @Valid @RequestBody InviteMemberRequest request) {

        UUID callerId = SecurityUtils.getCallerId();
        TeamMemberResponse response = teamService.inviteMember(teamId, callerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------------------------------------------------------------
    // DELETE /team-members/{teamMemberId}
    // -------------------------------------------------------------------------

    /**
     * Removes a member from the team. Only the team leader may do this.
     */
    @DeleteMapping("/team-members/{teamMemberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID teamMemberId) {

        UUID callerId = SecurityUtils.getCallerId();
        teamService.removeMember(teamMemberId, callerId);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // PATCH /team-members/{teamMemberId}/role
    // -------------------------------------------------------------------------

    /**
     * Changes a member's role. Leadership transfer is handled atomically.
     */
    @PatchMapping("/team-members/{teamMemberId}/role")
    public ResponseEntity<TeamMemberResponse> changeRole(
            @PathVariable UUID teamMemberId,
            @Valid @RequestBody ChangeRoleRequest request) {

        UUID callerId = SecurityUtils.getCallerId();
        TeamMemberResponse response = teamService.changeRole(teamMemberId, callerId, request);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // PATCH /team-members/{teamMemberId}/accept
    // -------------------------------------------------------------------------

    /**
     * Accepts a pending invitation. Only the invited user themselves may call this.
     */
    @PatchMapping("/team-members/{teamMemberId}/accept")
    public ResponseEntity<TeamMemberResponse> acceptInvitation(
            @PathVariable UUID teamMemberId) {

        UUID callerId = SecurityUtils.getCallerId();
        TeamMemberResponse response = teamService.acceptInvitation(teamMemberId, callerId);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // PATCH /team-members/{teamMemberId}/decline
    // -------------------------------------------------------------------------

    /**
     * Declines a pending invitation. Only the invited user themselves may call this.
     */
    @PatchMapping("/team-members/{teamMemberId}/decline")
    public ResponseEntity<TeamMemberResponse> declineInvitation(
            @PathVariable UUID teamMemberId) {

        UUID callerId = SecurityUtils.getCallerId();
        TeamMemberResponse response = teamService.declineInvitation(teamMemberId, callerId);
        return ResponseEntity.ok(response);
    }
}