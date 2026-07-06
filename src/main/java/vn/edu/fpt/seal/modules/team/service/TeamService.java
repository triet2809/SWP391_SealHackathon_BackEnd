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
import vn.edu.fpt.seal.modules.rules.entity.RuleAcceptance;
import vn.edu.fpt.seal.modules.rules.repository.EventRuleRepository;
import vn.edu.fpt.seal.modules.rules.repository.RuleAcceptanceRepository;
import vn.edu.fpt.seal.modules.team.dto.*;
import vn.edu.fpt.seal.modules.team.entity.*;
import vn.edu.fpt.seal.modules.team.mapper.TeamMapper;
import vn.edu.fpt.seal.modules.team.repository.*;
import vn.edu.fpt.seal.modules.teamtimeline.service.TeamTimelineService;
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
    private final TeamTimelineService timelineService;
    private final EventRuleRepository eventRuleRepository;
    private final RuleAcceptanceRepository ruleAcceptanceRepository;

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
        ensureRegistrationOpen(track);
        ensureTrackHasCapacity(track);
        String name = req.name().trim();
        if (teamRepository.existsByTrackIdAndNameIgnoreCase(track.getId(), name)) throw ApiException.conflict("Team name already exists in this track");

        boolean coordinator = isCoordinator(auth);
        // Thí sinh tự tạo đội phải chấp nhận thể lệ (rule PUBLIC) của sự kiện trước
        if (!coordinator) ensureRulesAccepted(currentUserId(auth), track, req.acceptedRules());
        Team team = teamRepository.save(Team.builder().track(track).name(name).status(TeamStatus.active).inviteCode(generateInviteCode()).build());
        Set<UUID> added = new LinkedHashSet<>();

        if (coordinator) {
            // Coordinator may create on behalf of others; leader/members optional
            // (they can build the roster incrementally). Hard cap at MAX_TEAM_SIZE.
            if (req.leaderUserId() != null) { addMemberInternal(team, req.leaderUserId(), TeamMemberRole.leader); added.add(req.leaderUserId()); }
            if (req.memberUserIds() != null) for (UUID id : req.memberUserIds()) if (added.add(id)) addMemberInternal(team, id, TeamMemberRole.member);
            for (UUID mid : resolveEmails(req.memberEmails())) if (added.add(mid)) addMemberInternal(team, mid, TeamMemberRole.member);
            if (added.size() > MAX_TEAM_SIZE) throw ApiException.badRequest("A team can have at most " + MAX_TEAM_SIZE + " members");
        } else {
            // A regular (non-coordinator) user creating their own team becomes the
            // leader. Solo creation is allowed (size 1); the roster grows later via
            // invite code or accepted join requests. Minimum size is enforced at a
            // later gate (registration close / submission), not at creation time.
            UUID callerId = currentUserId(auth);
            ensureNotInSameTerm(callerId, track);
            addMemberInternal(team, callerId, TeamMemberRole.leader); added.add(callerId);
            if (req.memberUserIds() != null) for (UUID id : req.memberUserIds()) if (added.add(id)) addMemberInternal(team, id, TeamMemberRole.member);
            for (UUID mid : resolveEmails(req.memberEmails())) if (added.add(mid)) addMemberInternal(team, mid, TeamMemberRole.member);
            if (added.size() > MAX_TEAM_SIZE)
                throw ApiException.badRequest("A team can have at most " + MAX_TEAM_SIZE + " members (including the leader)");
        }
        // Ghi mốc "đội được tạo" vào hành trình (timeline) của đội
        timelineService.record(team, null, TimelineEventType.TEAM_CREATED, "Team created",
                "Team '" + team.getName() + "' registered in track '" + track.getName() + "'");
        log.info("Team created: id={}, track={}, name={}, byCoordinator={}", team.getId(), track.getId(), team.getName(), coordinator);
        return toResponse(team);
    }

    /** Resolve member emails to user ids; each must be an existing registered user. */
    private List<UUID> resolveEmails(List<String> emails) {
        if (emails == null) return List.of();
        List<UUID> ids = new ArrayList<>();
        for (String raw : emails) {
            if (raw == null || raw.isBlank()) continue;
            String email = raw.toLowerCase().trim();
            User u = userRepository.findByEmail(email).orElseThrow(() -> ApiException.badRequest("No registered user with email: " + email));
            ids.add(u.getId());
        }
        return ids;
    }

    /** Generate a unique 6-char uppercase invite code. */
    private String generateInviteCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
            if (!teamRepository.existsByInviteCode(code)) return code;
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
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
    public TeamResponse moveToTrack(UUID id, MoveTeamTrackRequest req, Authentication auth) {
        Team team = findOrThrow(id);
        Track target = trackRepository.findById(req.trackId()).orElseThrow(() -> ApiException.notFound("Track not found: " + req.trackId()));
        Track current = team.getTrack();
        if (current.getId().equals(target.getId())) return toResponse(team);
        // Target track must be in the same event (cross-event moves are not allowed).
        if (!current.getEvent().getId().equals(target.getEvent().getId()))
            throw ApiException.badRequest("Target track must belong to the same event");
        ensureEditable(target);
        // Name must stay unique within the destination track.
        if (teamRepository.existsByTrackIdAndNameIgnoreCase(target.getId(), team.getName()))
            throw ApiException.conflict("A team with this name already exists in the target track");
        String oldTrack = current.getId().toString();
        team.setTrack(target);
        // Requirement #10-style audit trail: record cross-track moves.
        writeAudit(auth, team, AuditAction.PROMOTE_TEAM, oldTrack, target.getId().toString(), "Moved team to track " + target.getName());
        log.info("Team moved: id={}, from track={}, to track={}", team.getId(), oldTrack, target.getId());
        return toResponse(team);
    }

    @Transactional
    public TeamResponse joinByInviteCode(JoinTeamRequest req, Authentication auth) {
        UUID callerId = currentUserId(auth);
        String code = req.inviteCode().trim().toUpperCase().replace("SEAL-", "").replace("-", "");
        Team team = teamRepository.findByInviteCodeIgnoreCase(code)
                .orElseThrow(() -> ApiException.notFound("Team invite code not found"));
        ensureEditable(team.getTrack());
        ensureRegistrationOpen(team.getTrack());
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), callerId)) return toResponse(team);
        ensureNotInSameTerm(callerId, team.getTrack());
        // Người tham gia đội cũng phải chấp nhận thể lệ của sự kiện
        ensureRulesAccepted(callerId, team.getTrack(), req.acceptedRules());
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

    /**
     * A user leaves their own team while registration is still open. If the leader
     * leaves, leadership is handed to the earliest-joined remaining member; if no
     * members remain, the (now empty) team is deleted.
     * Returns the updated team, or null when the team was deleted.
     */
    @Transactional
    public TeamResponse leaveTeam(UUID teamId, Authentication auth) {
        UUID callerId = currentUserId(auth);
        Team team = findOrThrow(teamId);
        ensureRegistrationOpen(team.getTrack());
        TeamMember me = teamMemberRepository.findByTeamIdAndUserId(teamId, callerId)
                .orElseThrow(() -> ApiException.badRequest("You are not a member of this team"));
        boolean wasLeader = me.getRole() == TeamMemberRole.leader;
        teamMemberRepository.delete(me);
        teamMemberRepository.flush();

        List<TeamMember> remaining = teamMemberRepository.findByTeamIdOrderByRoleAscJoinedAtAsc(teamId);
        if (remaining.isEmpty()) {
            // Last person out: remove the empty team entirely.
            teamRepository.delete(team);
            log.info("Team {} deleted: last member {} left", teamId, callerId);
            return null;
        }
        if (wasLeader) {
            // Promote the earliest-joined remaining member to leader.
            TeamMember next = remaining.stream()
                    .min(java.util.Comparator.comparing(TeamMember::getJoinedAt))
                    .orElse(remaining.get(0));
            next.setRole(TeamMemberRole.leader);
            log.info("Team {} leadership transferred to {} after leader {} left", teamId, next.getUser().getId(), callerId);
        }
        return toResponse(team);
    }

    @Transactional
    public TeamResponse disqualify(UUID id, DisqualifyTeamRequest req, Authentication auth) {
        Team team = findOrThrow(id);
        String oldStatus = team.getStatus().name();
        team.setStatus(TeamStatus.disqualified);
        team.setDisqualifiedReason(req.reason().trim());
        // Requirement #10: disqualification must leave an audit trail.
        writeAudit(auth, team, AuditAction.DISQUALIFY_TEAM, oldStatus, TeamStatus.disqualified.name(), req.reason().trim());
        // Ghi mốc "đội bị loại" vào timeline
        timelineService.record(team, null, TimelineEventType.TEAM_DISQUALIFIED, "Team disqualified",
                "Reason: " + req.reason().trim(), null, null, TeamStatus.disqualified.name());
        return toResponse(team);
    }

    @Transactional
    public TeamResponse reactivate(UUID id) {
        Team team = findOrThrow(id); team.setStatus(TeamStatus.active); team.setDisqualifiedReason(null);
        // Ghi mốc "đội được khôi phục" vào timeline
        timelineService.record(team, null, TimelineEventType.TEAM_REACTIVATED, "Team reactivated",
                "Team was reactivated after disqualification", null, null, TeamStatus.active.name());
        return toResponse(team);
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

    /** A track may cap how many teams register into it (track.maxTeams). NULL = unlimited. */
    private void ensureTrackHasCapacity(Track track) {
        Integer max = track.getMaxTeams();
        if (max != null && teamRepository.countByTrackId(track.getId()) >= max) {
            throw ApiException.badRequest("Track is full (" + max + " teams max)");
        }
    }

    /**
     * A user may only take part in one event per term. Blocks joining/creating a team
     * when the user already belongs to an active team in a DIFFERENT event that shares
     * the same (non-blank) term.
     */
    private void ensureNotInSameTerm(UUID userId, Track targetTrack) {
        String term = targetTrack.getEvent().getTerm();
        if (term == null || term.isBlank()) return; // cannot compare without a term
        UUID targetEventId = targetTrack.getEvent().getId();
        for (TeamMember m : teamMemberRepository.findByUserIdOrderByJoinedAtDesc(userId)) {
            Team t = m.getTeam();
            if (t.getStatus() != TeamStatus.active) continue;
            var ev = t.getTrack().getEvent();
            if (ev.getId().equals(targetEventId)) continue; // same event is fine
            if (term.equalsIgnoreCase(ev.getTerm())) {
                throw ApiException.badRequest("You are already in a team for another event this term (" + ev.getTitle() + ")");
            }
        }
    }
    /**
     * Bắt buộc chấp nhận thể lệ (rule PUBLIC) trước khi đăng ký vào sự kiện:
     * - Sự kiện không có rule PUBLIC nào -> bỏ qua.
     * - Người dùng đã chấp nhận trước đó -> bỏ qua (idempotent).
     * - Ngược lại: acceptedRules phải là true, và ghi lại bản ghi chấp nhận.
     */
    private void ensureRulesAccepted(UUID userId, Track track, Boolean acceptedRules) {
        UUID eventId = track.getEvent().getId();
        if (!eventRuleRepository.existsByEventIdAndVisibility(eventId, RuleVisibility.PUBLIC)) return;
        if (ruleAcceptanceRepository.existsByUserIdAndEventId(userId, eventId)) return;
        if (!Boolean.TRUE.equals(acceptedRules)) {
            throw ApiException.badRequest("You must accept the event rules before registering");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User not found: " + userId));
        ruleAcceptanceRepository.save(RuleAcceptance.builder().user(user).event(track.getEvent()).build());
    }

    /** Team registration (create/join) is only allowed while the event has registration open (status=published). */
    private void ensureRegistrationOpen(Track track) { EventStatus s = track.getEvent().getStatus(); if (s != EventStatus.published) throw ApiException.badRequest("Registration is not open for this event (status: " + s + ")"); }
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
