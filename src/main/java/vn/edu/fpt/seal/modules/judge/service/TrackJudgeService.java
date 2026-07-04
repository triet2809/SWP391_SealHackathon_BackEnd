package vn.edu.fpt.seal.modules.judge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.AccountStatus;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.judge.dto.*;
import vn.edu.fpt.seal.modules.judge.entity.TrackJudge;
import vn.edu.fpt.seal.modules.judge.mapper.TrackJudgeMapper;
import vn.edu.fpt.seal.modules.judge.repository.TrackJudgeRepository;
import vn.edu.fpt.seal.modules.track.entity.Track;
import vn.edu.fpt.seal.modules.track.repository.TrackRepository;
import vn.edu.fpt.seal.modules.user.entity.User;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;

import java.util.UUID;

@Service @RequiredArgsConstructor
public class TrackJudgeService {
    private final TrackJudgeRepository trackJudgeRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<TrackJudgeResponse> list(UUID trackId, UUID userId, Pageable pageable) {
        if (trackId != null) return trackJudgeRepository.findByTrackId(trackId, pageable).map(TrackJudgeMapper::toResponse);
        if (userId != null) return trackJudgeRepository.findByUserId(userId, pageable).map(TrackJudgeMapper::toResponse);
        return trackJudgeRepository.findAll(pageable).map(TrackJudgeMapper::toResponse);
    }

    @Transactional
    public TrackJudgeResponse assign(AssignTrackJudgeRequest req) {
        Track track = trackRepository.findById(req.trackId()).orElseThrow(() -> ApiException.notFound("Track not found: " + req.trackId()));
        User user = userRepository.findById(req.userId()).orElseThrow(() -> ApiException.notFound("User not found: " + req.userId()));
        if (user.getStatus() != AccountStatus.approved) throw ApiException.badRequest("Only approved users can be assigned as judges");
        boolean hasJudgeRole = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "judge".equalsIgnoreCase(r.getName()));
        if (!hasJudgeRole) throw ApiException.badRequest("Assigned user must have judge role");
        if (trackJudgeRepository.existsByTrackIdAndUserId(track.getId(), user.getId())) throw ApiException.conflict("Judge already assigned to this track");
        if (trackJudgeRepository.existsByEventIdAndUserId(track.getEvent().getId(), user.getId())) throw ApiException.conflict("Judge already assigned in this event");
        return TrackJudgeMapper.toResponse(trackJudgeRepository.save(TrackJudge.builder().event(track.getEvent()).track(track).user(user).build()));
    }

    @Transactional public void remove(UUID id) { trackJudgeRepository.delete(trackJudgeRepository.findById(id).orElseThrow(() -> ApiException.notFound("Track judge assignment not found: " + id))); }
    @Transactional public void removeByTrackAndUser(UUID trackId, UUID userId) { trackJudgeRepository.delete(trackJudgeRepository.findByTrackIdAndUserId(trackId, userId).orElseThrow(() -> ApiException.notFound("Track judge assignment not found"))); }
}
