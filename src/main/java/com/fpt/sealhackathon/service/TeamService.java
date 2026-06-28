package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.enums.AuditAction;
import com.fpt.sealhackathon.dto.enums.TeamMemberRole;
import com.fpt.sealhackathon.dto.enums.TeamMemberStatus;
import com.fpt.sealhackathon.dto.enums.TeamStatus;
import com.fpt.sealhackathon.dto.request.ChangeRoleRequest;
import com.fpt.sealhackathon.dto.request.CreateTeamRequest;
import com.fpt.sealhackathon.dto.request.DisqualifyTeamRequest;
import com.fpt.sealhackathon.dto.request.InviteMemberRequest;
import com.fpt.sealhackathon.dto.request.LockTeamRequest;
import com.fpt.sealhackathon.dto.request.TeamListRequest;
import com.fpt.sealhackathon.dto.request.UpdateTeamRequest;
import com.fpt.sealhackathon.dto.response.PagedResponse;
import com.fpt.sealhackathon.dto.response.TeamDetailResponse;
import com.fpt.sealhackathon.dto.response.TeamMemberResponse;
import com.fpt.sealhackathon.dto.response.TeamSummaryResponse;
import com.fpt.sealhackathon.entity.AuditLog;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.Team;
import com.fpt.sealhackathon.entity.TeamMember;
import com.fpt.sealhackathon.entity.TeamProfile;
import com.fpt.sealhackathon.entity.User;
import com.fpt.sealhackathon.exception.BusinessException;
import com.fpt.sealhackathon.exception.ErrorCode;
import com.fpt.sealhackathon.mapper.TeamMapper;
import com.fpt.sealhackathon.repository.AuditLogRepository;
import com.fpt.sealhackathon.repository.TeamMemberRepository;
import com.fpt.sealhackathon.repository.TeamRepository;
import com.fpt.sealhackathon.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final String ROLE_COORDINATOR = "coordinator";
    private static final String TARGET_TYPE_TEAM = "team";

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final TeamMapper teamMapper;
    private final EntityManager entityManager;

    // -------------------------------------------------------------------------
    // GET /events/{eventId}/teams
    // -------------------------------------------------------------------------

    /**
     * Lists teams for an event with optional status filter and name search.
     * Read-only — no validation guards required.
     */
    @Transactional(readOnly = true)
    public PagedResponse<TeamSummaryResponse> listTeams(UUID eventId, TeamListRequest request) {

        // Step 1: Validate event exists
        Event event = findEventOrThrow(eventId);

        // Step 2: Query teams with filters
        Page<Team> page = teamRepository.findByEventIdFiltered(
                event.getId(),
                request.getStatus(),
                request.getSearch(),
                PageRequest.of(request.getPage(), request.getSize())
        );

        // Step 3: Map to summary responses
        List<TeamSummaryResponse> content = page.getContent().stream()
                .map(team -> {
                    long acceptedCount = teamMemberRepository
                            .countByTeamIdAndStatus(team.getId(), TeamMemberStatus.accepted.name());
                    return teamMapper.toSummaryResponse(team, (int) acceptedCount);
                })
                .toList();

        return PagedResponse.of(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    // -------------------------------------------------------------------------
    // POST /events/{eventId}/teams
    // -------------------------------------------------------------------------

    /**
     * Creates a new team and assigns the caller as the team leader.
     * The leader is automatically added as the first accepted member.
     */
    @Transactional
    public TeamDetailResponse createTeam(UUID eventId, UUID callerId, CreateTeamRequest request) {

        // Step 1: Validate event exists
        Event event = findEventOrThrow(eventId);

        // Step 2: Validate event registration is open
        validateRegistrationOpen(event);

        // Step 3: Validate caller is not already in a team for this event
        if (teamMemberRepository.existsActiveInEvent(eventId, callerId)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_IN_TEAM);
        }

        // Step 4: Validate team name uniqueness within event (case-insensitive)
        if (teamRepository.existsByEvent_IdAndNameIgnoreCase(eventId, request.getName())) {
            throw new BusinessException(ErrorCode.TEAM_NAME_DUPLICATE);
        }

        // Step 5: Create TeamProfile
        TeamProfile profile = TeamProfile.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        entityManager.persist(profile);

        // Step 6: Create Team
        User caller = findUserOrThrow(callerId);
        Team team = Team.builder()
                .event(event)
                .teamProfile(profile)
                .name(request.getName())
                .createdBy(caller)
                .status(TeamStatus.waiting_for_members)
                .build();
        team = teamRepository.save(team);

        /*
         * Team creator becomes the first member with LEADER role.
         * acceptedAt is set explicitly here — BEFORE trigger leaves non-null values unchanged.
         */
        TeamMember leader = TeamMember.builder()
                .team(team)
                .user(caller)
                .role(TeamMemberRole.leader)
                .status(TeamMemberStatus.accepted)
                .acceptedAt(Instant.now())
                .build();
        teamMemberRepository.save(leader);

        // Step 7: Refresh team to read trigger-updated status, approved_at, locked_at
        entityManager.flush();
        entityManager.refresh(team);

        // Step 8: Return full detail response
        Team teamWithMembers = teamRepository.findByIdWithMembers(team.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        return teamMapper.toDetailResponse(teamWithMembers);
    }

    // -------------------------------------------------------------------------
    // GET /teams/{teamId}
    // -------------------------------------------------------------------------

    /** Returns full team detail including all members. */
    @Transactional(readOnly = true)
    public TeamDetailResponse getTeam(UUID teamId) {

        // Step 1: Fetch team with members
        Team team = teamRepository.findByIdWithMembers(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        // Step 2: Map and return
        return teamMapper.toDetailResponse(team);
    }

    // -------------------------------------------------------------------------
    // PUT /teams/{teamId}
    // -------------------------------------------------------------------------

    /** Updates team name and/or description. Only the team leader may do this. */
    @Transactional
    public TeamDetailResponse updateTeam(UUID teamId, UUID callerId, UpdateTeamRequest request) {

        // Step 1: Validate team exists
        Team team = findTeamOrThrow(teamId);

        // Step 2: Validate caller is the active leader
        validateIsLeader(teamId, callerId);

        // Step 3: Validate event registration is open
        validateRegistrationOpen(team.getEvent());

        // Step 4: Validate team is not terminated
        validateNotTerminated(team);

        // Step 5: Validate team is not locked
        validateNotLocked(team);

        // Step 6: Validate new name uniqueness if name has changed
        if (!team.getName().equalsIgnoreCase(request.getName())) {
            if (teamRepository.existsByEvent_IdAndNameIgnoreCaseAndIdNot(
                    team.getEvent().getId(), request.getName(), teamId)) {
                throw new BusinessException(ErrorCode.TEAM_NAME_DUPLICATE);
            }
        }

        // Step 7: Apply update
        team.setName(request.getName());
        teamRepository.save(team);

        // Step 8: Return full detail response
        Team updated = teamRepository.findByIdWithMembers(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        return teamMapper.toDetailResponse(updated);
    }

    // -------------------------------------------------------------------------
    // DELETE /teams/{teamId}
    // -------------------------------------------------------------------------

    /** Deletes a team. Only permitted when status is WAITING_FOR_MEMBERS. */
    @Transactional
    public void deleteTeam(UUID teamId, UUID callerId) {

        // Step 1: Validate team exists
        Team team = findTeamOrThrow(teamId);

        // Step 2: Validate caller is the active leader
        validateIsLeader(teamId, callerId);

        // Step 3: Validate event registration is open
        validateRegistrationOpen(team.getEvent());

        // Step 4: Validate team is deletable — only waiting_for_members status allowed
        if (team.getStatus() != TeamStatus.waiting_for_members) {
            throw new BusinessException(ErrorCode.TEAM_NOT_DELETABLE);
        }

        // Capture the profile id before clearing the persistence context below.
        UUID profileId = team.getTeamProfile().getId();

        /*
         * Delete the member rows first. validateIsLeader() loaded the leader
         * TeamMember into the persistence context; deleting the team while that
         * managed member still references it fails the flush with
         * TransientPropertyValueException (JPA doesn't know about the DB-level
         * ON DELETE CASCADE). A bulk delete removes the rows, then clear()
         * detaches any stale managed member/team copies.
         */
        teamMemberRepository.deleteByTeamId(teamId);
        entityManager.flush();
        entityManager.clear();

        /*
         * Delete order matters: the team must go before its profile because
         * teams.team_profile_id is ON DELETE RESTRICT.
         */
        teamRepository.deleteById(teamId);
        entityManager.flush();

        TeamProfile profile = entityManager.find(TeamProfile.class, profileId);
        if (profile != null) {
            entityManager.remove(profile);
        }
    }

    // -------------------------------------------------------------------------
    // GET /teams/{teamId}/members
    // -------------------------------------------------------------------------

    /** Lists members of a team, optionally filtered by status. */
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> listMembers(UUID teamId, TeamMemberStatus status) {

        // Step 1: Validate team exists
        findTeamOrThrow(teamId);

        // Step 2: Query members with optional status filter
        List<TeamMember> members = teamMemberRepository.findByTeamIdFiltered(teamId,
                status != null ? status.name() : null);

        // Step 3: Map and return
        return teamMapper.toMemberResponseList(members);
    }

    // -------------------------------------------------------------------------
    // POST /teams/{teamId}/members/invite
    // -------------------------------------------------------------------------

    /** Invites a user to the team. Only the team leader may send invitations. */
    @Transactional
    public TeamMemberResponse inviteMember(UUID teamId, UUID callerId, InviteMemberRequest request) {

        // Step 1: Validate team exists (with event eagerly loaded)
        Team team = findTeamOrThrow(teamId);

        // Step 2: Validate caller is the active leader
        validateIsLeader(teamId, callerId);

        // Step 3: Validate event registration is open
        validateRegistrationOpen(team.getEvent());

        // Step 4: Validate team is not terminated
        validateNotTerminated(team);

        // Step 5: Validate team is not locked
        validateNotLocked(team);

        // Step 6: Validate team capacity (invited + accepted < max_team_size)
        long activeCount = teamMemberRepository.countActiveByTeamId(teamId);
        if (activeCount >= team.getEvent().getMaxTeamSize()) {
            throw new BusinessException(ErrorCode.TEAM_FULL);
        }

        // Step 7: Validate caller is not inviting themselves
        if (callerId.equals(request.getUserId())) {
            throw new BusinessException(ErrorCode.CANNOT_SELF_INVITE);
        }

        // Step 8: Validate target user exists and is approved
        User targetUser = findUserOrThrow(request.getUserId());
        // Note: account_status check requires User stub to be expanded by BE2
        // validateUserApproved(targetUser);

        // Step 9: Validate target user not already active in any team this event
        if (teamMemberRepository.existsActiveInEvent(team.getEvent().getId(), request.getUserId())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_IN_TEAM);
        }

        // Step 10: Persist invite row — BEFORE trigger stamps joined_at
        TeamMember invite = TeamMember.builder()
                .team(team)
                .user(targetUser)
                .role(TeamMemberRole.member)
                .status(TeamMemberStatus.invited)
                .build();
        TeamMember saved = teamMemberRepository.save(invite);

        // Step 11: Refresh team — AFTER trigger may have updated teams.status
        entityManager.flush();
        entityManager.refresh(team);

        return teamMapper.toMemberResponse(saved);
    }

    // -------------------------------------------------------------------------
    // DELETE /team-members/{teamMemberId}
    // -------------------------------------------------------------------------

    /** Removes an invited or accepted member from the team. */
    @Transactional
    public void removeMember(UUID teamMemberId, UUID callerId) {

        // Step 1: Validate team member exists with team and event loaded
        TeamMember target = teamMemberRepository.findByIdWithTeamAndUser(teamMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team team = target.getTeam();

        // Step 2: Validate caller is the active leader
        validateIsLeader(team.getId(), callerId);

        // Step 3: Validate leader is not removing themselves
        if (target.getUser().getId().equals(callerId)) {
            throw new BusinessException(ErrorCode.CANNOT_SELF_REMOVE);
        }

        // Step 4: Validate target member has an active status
        if (!target.isActive()) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_INVALID);
        }

        // Step 5: Validate event registration is open
        validateRegistrationOpen(team.getEvent());

        // Step 6: Validate team is not terminated
        validateNotTerminated(team);

        // Step 7: Validate team is not locked
        validateNotLocked(team);

        /*
         * Step 8: Set status to REMOVED.
         * removedAt is set explicitly here; BEFORE trigger leaves non-null values unchanged.
         */
        target.setStatus(TeamMemberStatus.removed);
        target.setRemovedAt(Instant.now());
        teamMemberRepository.save(target);

        // Step 9: Refresh team — AFTER trigger may drop team status (e.g. to waiting_for_members)
        entityManager.flush();
        entityManager.refresh(team);
    }

    // -------------------------------------------------------------------------
    // PATCH /team-members/{teamMemberId}/role
    // -------------------------------------------------------------------------

    /**
     * Changes a member's role. If new role is LEADER, the current leader is
     * atomically demoted to MEMBER before the target is promoted.
     */
    @Transactional
    public TeamMemberResponse changeRole(UUID teamMemberId, UUID callerId, ChangeRoleRequest request) {

        // Step 1: Validate team member exists with team and event loaded
        TeamMember target = teamMemberRepository.findByIdWithTeamAndUser(teamMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        Team team = target.getTeam();

        // Step 2: Validate caller is the active leader
        validateIsLeader(team.getId(), callerId);

        // Step 3: Validate caller is not targeting themselves
        if (target.getUser().getId().equals(callerId)) {
            throw new BusinessException(ErrorCode.CANNOT_CHANGE_OWN_ROLE);
        }

        // Step 4: Validate target is an accepted member
        if (target.getStatus() != TeamMemberStatus.accepted) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_INVALID);
        }

        // Step 5: Validate event registration is open
        validateRegistrationOpen(team.getEvent());

        // Step 6: Validate team is not terminated
        validateNotTerminated(team);

        // Step 7: Validate team is not locked
        validateNotLocked(team);

        /*
         * Step 8: If promoting to LEADER, atomically demote current leader first.
         * This preserves uq_team_members_one_active_leader (only one active leader).
         */
        if (request.getRole() == TeamMemberRole.leader) {
            teamMemberRepository.findActiveLeaderByTeamId(team.getId())
                    .ifPresent(currentLeader -> {
                        currentLeader.setRole(TeamMemberRole.member);
                        teamMemberRepository.save(currentLeader);
                    });
        }

        // Step 9: Apply new role to target
        target.setRole(request.getRole());
        TeamMember saved = teamMemberRepository.save(target);

        return teamMapper.toMemberResponse(saved);
    }

    // -------------------------------------------------------------------------
    // PATCH /team-members/{teamMemberId}/accept
    // -------------------------------------------------------------------------

    /** Accepts a pending invitation. Only the invited user themselves may call this. */
    @Transactional
    public TeamMemberResponse acceptInvitation(UUID teamMemberId, UUID callerId) {

        // Step 1: Validate team member exists with team and event loaded
        TeamMember member = teamMemberRepository.findByIdWithTeamAndUser(teamMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        // Step 2: Validate caller owns this membership row
        if (!member.getUser().getId().equals(callerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_NOT_MEMBER_OWNER);
        }

        // Step 3: Validate current status is INVITED
        if (member.getStatus() != TeamMemberStatus.invited) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_INVALID);
        }

        Team team = member.getTeam();

        // Step 4: Validate event registration is open
        validateRegistrationOpen(team.getEvent());

        // Step 5: Validate team is not terminated
        validateNotTerminated(team);

        // Step 6: Validate team is not locked
        validateNotLocked(team);

        // Step 7: Validate team still has an open accepted slot (race condition guard)
        long acceptedCount = teamMemberRepository
                .countByTeamIdAndStatus(team.getId(), TeamMemberStatus.accepted.name());
        if (acceptedCount >= team.getEvent().getMaxTeamSize()) {
            throw new BusinessException(ErrorCode.TEAM_FULL);
        }

        /*
         * Step 8: Accept the invitation.
         * acceptedAt set explicitly; BEFORE trigger leaves non-null values unchanged.
         */
        member.setStatus(TeamMemberStatus.accepted);
        member.setAcceptedAt(Instant.now());
        TeamMember saved = teamMemberRepository.save(member);

        // Step 9: Refresh team — AFTER trigger may advance team status
        entityManager.flush();
        entityManager.refresh(team);

        return teamMapper.toMemberResponse(saved);
    }

    // -------------------------------------------------------------------------
    // PATCH /team-members/{teamMemberId}/decline
    // -------------------------------------------------------------------------

    /**
     * Declines a pending invitation. Only the invited user themselves may call this.
     * No registration-open check — declining always permitted (freeing a slot).
     */
    @Transactional
    public TeamMemberResponse declineInvitation(UUID teamMemberId, UUID callerId) {

        // Step 1: Validate team member exists with team loaded
        TeamMember member = teamMemberRepository.findByIdWithTeamAndUser(teamMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        // Step 2: Validate caller owns this membership row
        if (!member.getUser().getId().equals(callerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_NOT_MEMBER_OWNER);
        }

        // Step 3: Validate current status is INVITED
        if (member.getStatus() != TeamMemberStatus.invited) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_INVALID);
        }

        /*
         * Step 4: Decline the invitation.
         * declinedAt set explicitly; BEFORE trigger leaves non-null values unchanged.
         * No registration-open check: freeing a slot is always permitted.
         */
        member.setStatus(TeamMemberStatus.declined);
        member.setDeclinedAt(Instant.now());
        TeamMember saved = teamMemberRepository.save(member);

        // Step 5: Refresh team — AFTER trigger may regress team status
        entityManager.flush();
        entityManager.refresh(member.getTeam());

        return teamMapper.toMemberResponse(saved);
    }

    // -------------------------------------------------------------------------
    // PATCH /teams/{teamId}/lock
    // -------------------------------------------------------------------------

    /** Locks a team so no further member changes are allowed. Coordinator only. */
    @Transactional
    public TeamDetailResponse lockTeam(UUID teamId, UUID callerId, LockTeamRequest request) {

        // Step 1: Validate team exists
        Team team = findTeamOrThrow(teamId);

        // Step 2: Validate caller is a coordinator
        validateIsCoordinator(callerId);

        // Step 3: Cannot lock a terminated team
        validateNotTerminated(team);

        // Step 4: Reject if already locked
        if (team.isLocked()) {
            throw new BusinessException(ErrorCode.TEAM_ALREADY_LOCKED);
        }

        UUID eventId = team.getEvent().getId();
        String reason = (request != null && request.getReason() != null)
                ? request.getReason()
                : "Locked by coordinator";

        // Step 5: Apply lock via native update (locked_at/reason are read-only on the entity)
        teamRepository.lockTeam(teamId, reason);

        // Step 6: Audit
        writeTeamAudit(callerId, eventId, teamId, AuditAction.LOCK_TEAM, reason);

        // Step 7: Return refreshed detail
        return getTeam(teamId);
    }

    // -------------------------------------------------------------------------
    // PATCH /teams/{teamId}/disqualify
    // -------------------------------------------------------------------------

    /** Disqualifies a team (terminal state). Coordinator only. */
    @Transactional
    public TeamDetailResponse disqualifyTeam(UUID teamId, UUID callerId, DisqualifyTeamRequest request) {

        // Step 1: Validate team exists
        Team team = findTeamOrThrow(teamId);

        // Step 2: Validate caller is a coordinator
        validateIsCoordinator(callerId);

        // Step 3: Cannot disqualify an already terminated team
        validateNotTerminated(team);

        UUID eventId = team.getEvent().getId();

        // Step 4: Apply disqualify via native update (status is read-only on the entity)
        teamRepository.disqualifyTeam(teamId, request.getReason());

        // Step 5: Audit
        writeTeamAudit(callerId, eventId, teamId, AuditAction.DISQUALIFY_TEAM, request.getReason());

        // Step 6: Return refreshed detail
        return getTeam(teamId);
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    /*
     * Validate event registration is open.
     */
    private void validateRegistrationOpen(Event event) {
        if (event.getStatus() != Event.EventStatus.registration_open) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_OPEN);
        }
    }

    /*
     * Validate team is not in a terminal state (ELIMINATED or DISQUALIFIED).
     */
    private void validateNotTerminated(Team team) {
        if (team.isTerminated()) {
            throw new BusinessException(ErrorCode.TEAM_TERMINATED);
        }
    }

    /*
     * Validate team is not locked (locked_at IS NOT NULL).
     */
    private void validateNotLocked(Team team) {
        if (team.isLocked()) {
            throw new BusinessException(ErrorCode.TEAM_LOCKED);
        }
    }

    /*
     * Validate caller is the active leader of the team.
     */
    private void validateIsLeader(UUID teamId, UUID callerId) {
        // No active leader => the caller is provably not the leader. Report
        // FORBIDDEN_NOT_LEADER rather than TEAM_NOT_FOUND: callers reach this only
        // after the team has already been confirmed to exist, so a 404 here is
        // misleading (e.g. a seeded team that has no leader member row).
        TeamMember leader = teamMemberRepository
                .findActiveLeaderByTeamId(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_NOT_LEADER));
        if (!leader.getUser().getId().equals(callerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_NOT_LEADER);
        }
    }

    /*
     * Validate caller holds the coordinator role.
     */
    private void validateIsCoordinator(UUID callerId) {
        if (!userRepository.hasRole(callerId, ROLE_COORDINATOR)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_NOT_COORDINATOR);
        }
    }

    /*
     * Write an audit_logs row for a team action.
     */
    private void writeTeamAudit(UUID callerId, UUID eventId, UUID teamId,
                                AuditAction action, String details) {
        AuditLog log = AuditLog.builder()
                .userId(callerId)
                .eventId(eventId)
                .teamId(teamId)
                .action(action)
                .targetType(TARGET_TYPE_TEAM)
                .targetId(teamId)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    /*
     * Find event by ID or throw EVENT_NOT_FOUND.
     */
    private Event findEventOrThrow(UUID eventId) {
        Event event = entityManager.find(Event.class, eventId);
        if (event == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }
        return event;
    }

    /*
     * Find team by ID or throw TEAM_NOT_FOUND.
     */
    private Team findTeamOrThrow(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
    }

    /*
     * Return a User proxy reference by ID or throw USER_NOT_FOUND.
     * Uses entityManager.getReference() to avoid a SELECT when only the FK is needed.
     */
    private User findUserOrThrow(UUID userId) {
        User ref = entityManager.find(User.class, userId);
        if (ref == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return ref;
    }
}