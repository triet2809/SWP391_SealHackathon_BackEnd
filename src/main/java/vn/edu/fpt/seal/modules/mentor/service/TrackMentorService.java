package vn.edu.fpt.seal.modules.mentor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.AccountStatus;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.mentor.dto.*;
import vn.edu.fpt.seal.modules.mentor.entity.TrackMentor;
import vn.edu.fpt.seal.modules.mentor.mapper.TrackMentorMapper;
import vn.edu.fpt.seal.modules.mentor.repository.TrackMentorRepository;
import vn.edu.fpt.seal.modules.track.entity.Track;
import vn.edu.fpt.seal.modules.track.repository.TrackRepository;
import vn.edu.fpt.seal.modules.user.entity.User;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;

import java.util.*;

@Service @RequiredArgsConstructor
public class TrackMentorService {
    private final TrackMentorRepository trackMentorRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<TrackMentorResponse> list(UUID trackId, UUID userId, Pageable pageable) {
        if (trackId != null) return trackMentorRepository.findByTrackId(trackId, pageable).map(TrackMentorMapper::toResponse);
        if (userId != null) return trackMentorRepository.findByUserId(userId, pageable).map(TrackMentorMapper::toResponse);
        return trackMentorRepository.findAll(pageable).map(TrackMentorMapper::toResponse);
    }

    @Transactional
    public TrackMentorResponse assign(AssignTrackMentorRequest req) {
        Track track = trackRepository.findById(req.trackId()).orElseThrow(() -> ApiException.notFound("Track not found: " + req.trackId()));
        User user = userRepository.findById(req.userId()).orElseThrow(() -> ApiException.notFound("User not found: " + req.userId()));
        if (user.getStatus() != AccountStatus.approved) throw ApiException.badRequest("Only approved users can be assigned as mentors");
        boolean hasMentorRole = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "mentor".equalsIgnoreCase(r.getName()));
        if (!hasMentorRole) throw ApiException.badRequest("Assigned user must have mentor role");
        if (trackMentorRepository.existsByTrackIdAndUserId(track.getId(), user.getId())) throw ApiException.conflict("Mentor already assigned to this track");
        if (trackMentorRepository.existsByEventIdAndUserId(track.getEvent().getId(), user.getId())) throw ApiException.conflict("Mentor already assigned in this event");
        return TrackMentorMapper.toResponse(trackMentorRepository.save(TrackMentor.builder().event(track.getEvent()).track(track).user(user).build()));
    }

    @Transactional public void remove(UUID id) { trackMentorRepository.delete(trackMentorRepository.findById(id).orElseThrow(() -> ApiException.notFound("Track mentor assignment not found: " + id))); }
    @Transactional public void removeByTrackAndUser(UUID trackId, UUID userId) { trackMentorRepository.delete(trackMentorRepository.findByTrackIdAndUserId(trackId, userId).orElseThrow(() -> ApiException.notFound("Track mentor assignment not found"))); }

    @Transactional(readOnly = true)
    public List<MentorTeamResponse> teams(UUID mentorId) {
        return trackMentorRepository.findTeamRowsForMentor(mentorId).stream().map(row -> MentorTeamResponse.builder()
                .trackMentorId(row.getTrackMentorId()).mentorId(row.getMentorId()).trackId(row.getTrackId()).trackName(row.getTrackName())
                .roundId(row.getRoundId()).roundName(row.getRoundName()).teamId(row.getTeamId()).teamName(row.getTeamName()).teamStatus(row.getTeamStatus()).build()).toList();
    }
}
