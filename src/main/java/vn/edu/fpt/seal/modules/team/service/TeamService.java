package vn.edu.fpt.seal.modules.team.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.*;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.audit.entity.AuditLog;
import vn.edu.fpt.seal.modules.audit.repository.AuditLogRepository;
import vn.edu.fpt.seal.modules.team.dto.*;
import vn.edu.fpt.seal.modules.team.entity.*;
import vn.edu.fpt.seal.modules.team.mapper.TeamMapper;
import vn.edu.fpt.seal.modules.team.repository.*;
import vn.edu.fpt.seal.security.CurrentUser;
import vn.edu.fpt.seal.modules.track.entity.Track;
import vn.edu.fpt.seal.modules.track.repository.TrackRepository;
import vn.edu.fpt.seal.modules.user.entity.User;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {
    /** Hackathon teams must have between MIN and MAX members (requirement #2). */
    private static final int MIN_TEAM_SIZE = 3;
    private static final int MAX_TEAM_SIZE = 5;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public Page<TeamResponse> listByTrack(UUID trackId, Pageable pageable) {
        if (trackId == null) return teamRepository.findAll(pageable).map(this::toResponse);
        if (!trackRepository.existsById(trackId)) throw ApiException.notFound("Track not found: " + trackId);
        return teamRepository.findByTrackId(trackId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TeamResponse get(UUID id) { return toResponse(findOrThrow(id)); }

    @Transactional(readOnly = true)
    public List<TeamResponse> myTeams(Authentication auth) {
        UUID callerId = currentUserId(auth);
        return teamMemberRepository.findByUserIdOrderByJoinedAtDesc(callerId).stream()
                .map(TeamMember::getTeam)
                .distinct()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TeamResponse create(CreateTeamRequest req, Authentication auth) {
        Track track = trackRepository.findById(req.trackId()).orElseThrow(() -> ApiException.notFound("Track not found: " + req.trackId()));
        ensureEditable(track);
        String name = req.name().trim();
        if (teamRepository.existsByTrackIdAndNameIgnoreCase(track.getId(), name)) throw ApiException.conflict("Team name already exists in this track");

        boolean coordinator = isCoordinator(auth);
        Team team = teamRepository.save(Team.builder().track(track).name(name).status(TeamStatus.active).build());
        Set<UUID> added = new LinkedHashSet<>();

        if (coordinator) {
            // Coordinator may create on behalf of others; leader/members optional
            // (they can build the roster incrementally). Hard cap at MAX_TEAM_SIZE.
            if (req.leaderUserId() != null) { addMemberInternal(team, req.leaderUserId(), TeamMemberRole.leader); added.add(req.leaderUserId()); }
            if (req.memberUserIds() != null) for (UUID id : req.memberUserIds()) if (added.add(id)) addMemberInternal(team, id, TeamMemberRole.member);
            if (added.size() > MAX_TEAM_SIZE) throw ApiException.badRequest("A team can have at most " + MAX_TEAM_SIZE + " members");
        } else {
            // A regular (non-coordinator) user creating their own team becomes the
            // leader. Self-organised teams must satisfy the 3-5 size rule up front.
            UUID callerId = currentUserId(auth);
            addMemberInternal(team, callerId, TeamMemberRole.leader); added.add(callerId);
            if (req.memberUserIds() != null) for (UUID id : req.memberUserIds()) if (added.add(id)) addMemberInternal(team, id, TeamMemberRole.member);
            if (added.size() < MIN_TEAM_SIZE || added.size() > MAX_TEAM_SIZE)
                throw ApiException.badRequest("A self-created team must have between " + MIN_TEAM_SIZE + " and " + MAX_TEAM_SIZE + " members (including the leader)");
        }
        log.info("Team created: id={}, track={}, name={}, byCoordinator={}", team.getId(), track.getId(), team.getName(), coordinator);
        return toResponse(team);
    }

    @Transactional
    public TeamResponse update(UUID id, UpdateTeamRequest req) {
        Team team = findOrThrow(id); ensureEditable(team.getTrack());
        if (req.name() != null) {
            String name = req.name().trim();
            if (!name.equalsIgnoreCase(team.getName()) && teamRepository.existsByTrackIdAndNameIgnoreCase(team.getTrack().getId(), name)) throw ApiException.conflict("Team name already exists in this track");
            team.setName(name);
        }
        return toResponse(team);
    }

    @Transactional
    public TeamResponse joinByInviteCode(JoinTeamRequest req, Authentication auth) {
        UUID callerId = currentUserId(auth);
        String code = req.inviteCode().trim().toLowerCase().replace("seal-", "").replace("-", "");
        Team team = teamRepository.findAll().stream()
                .filter(t -> t.getId().toString().replace("-", "").toLowerCase().startsWith(code))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Team invite code not found"));
        ensureEditable(team.getTrack());
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), callerId)) return toResponse(team);
        if (teamMemberRepository.countByTeamId(team.getId()) >= MAX_TEAM_SIZE) throw ApiException.badRequest("A team can have at most " + MAX_TEAM_SIZE + " members");
        addMemberInternal(team, callerId, TeamMemberRole.member);
        return toResponse(team);
    }

    @Transactional
    public TeamResponse addMember(UUID teamId, AddTeamMemberRequest req) {
        Team team = findOrThrow(teamId); ensureEditable(team.getTrack());
        if (teamMemberRepository.countByTeamId(teamId) >= MAX_TEAM_SIZE)
            throw ApiException.badRequest("A team can have at most " + MAX_TEAM_SIZE + " members");
        addMemberInternal(team, req.userId(), req.role() == null ? TeamMemberRole.member : req.role());
        return toResponse(team);
    }

    @Transactional
    public void removeMember(UUID teamId, UUID userId) {
        Team team = findOrThrow(teamId); ensureEditable(team.getTrack());
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId).orElseThrow(() -> ApiException.notFound("Team member not found"));
        teamMemberRepository.delete(member);
    }

    @Transactional
    public TeamResponse disqualify(UUID id, DisqualifyTeamRequest req, Authentication auth) {
        Team team = findOrThrow(id);
        String oldStatus = team.getStatus().name();
        team.setStatus(TeamStatus.disqualified);
        team.setDisqualifiedReason(req.reason().trim());
        // Requirement #10: disqualification must leave an audit trail.
        writeAudit(auth, team, AuditAction.DISQUALIFY_TEAM, oldStatus, TeamStatus.disqualified.name(), req.reason().trim());
        return toResponse(team);
    }

    @Transactional
    public TeamResponse reactivate(UUID id) {
        Team team = findOrThrow(id); team.setStatus(TeamStatus.active); team.setDisqualifiedReason(null); return toResponse(team);
    }

    @Transactional
    public void delete(UUID id) { Team team = findOrThrow(id); ensureDraft(team.getTrack()); teamRepository.delete(team); }

    private TeamResponse toResponse(Team team) { return TeamMapper.toResponse(team, teamMemberRepository.findByTeamIdOrderByRoleAscJoinedAtAsc(team.getId())); }
    private Team findOrThrow(UUID id) { return teamRepository.findWithTrackById(id).orElseThrow(() -> ApiException.notFound("Team not found: " + id)); }
    private TeamMember addMemberInternal(Team team, UUID userId, TeamMemberRole role) {
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), userId)) throw ApiException.conflict("User already belongs to this team");
        if (role == TeamMemberRole.leader && teamMemberRepository.existsByTeamIdAndRole(team.getId(), TeamMemberRole.leader)) throw ApiException.conflict("Team already has a leader");
        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User not found: " + userId));
        if (user.getStatus() != AccountStatus.approved) throw ApiException.badRequest("Only approved users can join teams");
        return teamMemberRepository.save(TeamMember.builder().team(team).user(user).role(role).build());
    }
    private void ensureEditable(Track track) { EventStatus s = track.getEvent().getStatus(); if (s == EventStatus.completed || s == EventStatus.cancelled) throw ApiException.badRequest("Cannot edit teams in event status " + s); }
    private void ensureDraft(Track track) { EventStatus s = track.getEvent().getStatus(); if (s != EventStatus.draft) throw ApiException.badRequest("Teams can only be deleted while event is draft (current: " + s + ")"); }

    private boolean isCoordinator(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDINATOR"));
    }
    private UUID currentUserId(Authentication auth) {
        UUID id = currentUserIdOrNull(auth);
        if (id == null) throw ApiException.forbidden("Authentication required");
        return id;
    }
    private UUID currentUserIdOrNull(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof CurrentUser c) return c.getId();
        return null;
    }
    private void writeAudit(Authentication auth, Team team, AuditAction action, String oldValue, String newValue, String details) {
        UUID actorId = currentUserIdOrNull(auth);
        User actor = actorId == null ? null : userRepository.findById(actorId).orElse(null);
        auditLogRepository.save(AuditLog.builder()
                .user(actor)
                .team(team)
                .action(action)
                .targetType("team")
                .targetId(team.getId())
                .oldValue(oldValue)
                .newValue(newValue)
                .details(details)
                .build());
    }
}
