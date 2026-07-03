package vn.edu.fpt.seal.modules.submission.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.EventStatus;
import vn.edu.fpt.seal.common.enums.TeamStatus;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.round.repository.RoundRepository;
import vn.edu.fpt.seal.modules.submission.dto.*;
import vn.edu.fpt.seal.modules.submission.entity.Submission;
import vn.edu.fpt.seal.modules.submission.mapper.SubmissionMapper;
import vn.edu.fpt.seal.modules.submission.repository.SubmissionRepository;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.team.repository.TeamMemberRepository;
import vn.edu.fpt.seal.modules.team.repository.TeamRepository;
import vn.edu.fpt.seal.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final RoundRepository roundRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    @Transactional(readOnly = true)
    public Page<SubmissionResponse> list(UUID roundId, UUID teamId, UUID trackId, Pageable pageable) {
        return submissionRepository.search(roundId, teamId, trackId, pageable).map(SubmissionMapper::toResponse);
    }
    @Transactional(readOnly = true) public SubmissionResponse get(UUID id) { return SubmissionMapper.toResponse(findOrThrow(id)); }
    @Transactional
    public SubmissionResponse submit(UpsertSubmissionRequest req, Authentication auth) {
        Round round = roundRepository.findById(req.roundId()).orElseThrow(() -> ApiException.notFound("Round not found: " + req.roundId()));
        Team team = teamRepository.findWithTrackById(req.teamId()).orElseThrow(() -> ApiException.notFound("Team not found: " + req.teamId()));
        validateRoundTeam(round, team); ensureCanSubmit(team, auth); ensureSubmissionOpen(round);
        Submission s = submissionRepository.findByRoundIdAndTeamId(round.getId(), team.getId()).orElseGet(() -> Submission.builder().round(round).team(team).build());
        apply(s, req.repoUrl(), req.demoUrl(), req.slideUrl(), req.reportUrl(), req.apiMetadata());
        s = submissionRepository.save(s);
        log.info("Submission upserted: id={}, round={}, team={}", s.getId(), round.getId(), team.getId());
        return SubmissionMapper.toResponse(s);
    }
    @Transactional
    public SubmissionResponse update(UUID id, UpdateSubmissionRequest req, Authentication auth) {
        Submission s = findOrThrow(id); ensureCanSubmit(s.getTeam(), auth); ensureSubmissionOpen(s.getRound());
        apply(s, req.repoUrl(), req.demoUrl(), req.slideUrl(), req.reportUrl(), req.apiMetadata()); return SubmissionMapper.toResponse(s);
    }
    @Transactional public void delete(UUID id) { Submission s = findOrThrow(id); ensureSubmissionOpen(s.getRound()); submissionRepository.delete(s); }
    private Submission findOrThrow(UUID id) { return submissionRepository.findWithRelationsById(id).orElseThrow(() -> ApiException.notFound("Submission not found: " + id)); }
    private void apply(Submission s, String repoUrl, String demoUrl, String slideUrl, String reportUrl, String apiMetadata) {
        if (repoUrl != null) s.setRepoUrl(blankToNull(repoUrl)); if (demoUrl != null) s.setDemoUrl(blankToNull(demoUrl));
        if (slideUrl != null) s.setSlideUrl(blankToNull(slideUrl)); if (reportUrl != null) s.setReportUrl(blankToNull(reportUrl)); if (apiMetadata != null) s.setApiMetadata(blankToNull(apiMetadata));
    }
    private void validateRoundTeam(Round round, Team team) {
        if (!round.getTrack().getId().equals(team.getTrack().getId())) throw ApiException.badRequest("Team must belong to the same track as round");
        if (team.getStatus() == TeamStatus.disqualified) throw ApiException.badRequest("Disqualified team cannot submit");
        EventStatus status = round.getTrack().getEvent().getStatus(); if (status == EventStatus.completed || status == EventStatus.cancelled) throw ApiException.badRequest("Cannot submit in event status " + status);
    }
    private void ensureSubmissionOpen(Round round) { if (LocalDateTime.now().isAfter(round.getSubmissionDeadline())) throw ApiException.badRequest("Submission deadline has passed"); }
    private void ensureCanSubmit(Team team, Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) throw ApiException.forbidden("Authentication required");
        boolean coordinator = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDINATOR")); if (coordinator) return;
        if (!(auth.getPrincipal() instanceof CurrentUser currentUser)) throw ApiException.forbidden("Invalid principal");
        if (!teamMemberRepository.existsByTeamIdAndUserId(team.getId(), currentUser.getId())) throw ApiException.forbidden("Only team members can submit for this team");
    }
    private String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
