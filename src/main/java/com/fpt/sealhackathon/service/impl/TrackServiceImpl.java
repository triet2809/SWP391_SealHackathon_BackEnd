package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.roundtrack.RoundTrackRequest;
import com.fpt.sealhackathon.dto.roundtrack.RoundTrackResponse;
import com.fpt.sealhackathon.entity.Track;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.repository.TrackRepository;
import com.fpt.sealhackathon.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;

    @Override
    public RoundTrackResponse createTrack(RoundTrackRequest request) {
        validateRoundExists(request.getRoundId(), request.getEventId());
        validateDuplicateTrack(request);
        validateTrackRules(request);

        Track track = Track.builder()
                .eventId(request.getEventId())
                .roundId(request.getRoundId())
                .name(request.getName())
                .challengeTitle(request.getChallengeTitle())
                .challengeDescription(request.getChallengeDescription())
                .challengeFileUrl(request.getChallengeFileUrl())
                .maxTeams(request.getMaxTeams())
                .topNToPromote(request.getTopNToPromote())
                .displayOrder(request.getDisplayOrder())
                .isFinalSharedTrack(request.getIsFinalSharedTrack())
                .createdBy(request.getCreatedBy())
                .build();

        return mapToResponse(trackRepository.save(track));
    }

    @Override
    public List<RoundTrackResponse> getAllTracks() {
        return trackRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public RoundTrackResponse getTrackById(UUID id) {
        return mapToResponse(findTrackById(id));
    }

    @Override
    public List<RoundTrackResponse> getTracksByRoundId(UUID roundId) {
        return trackRepository.findByRoundIdOrderByDisplayOrderAscNameAsc(roundId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RoundTrackResponse updateTrack(UUID id, RoundTrackRequest request) {
        Track track = findTrackById(id);

        validateRoundExists(request.getRoundId(), request.getEventId());
        validateDuplicateTrackForUpdate(id, request);
        validateTrackRules(request);

        track.setEventId(request.getEventId());
        track.setRoundId(request.getRoundId());
        track.setName(request.getName());
        track.setChallengeTitle(request.getChallengeTitle());
        track.setChallengeDescription(request.getChallengeDescription());
        track.setChallengeFileUrl(request.getChallengeFileUrl());
        track.setMaxTeams(request.getMaxTeams());
        track.setTopNToPromote(request.getTopNToPromote());
        track.setDisplayOrder(request.getDisplayOrder());
        track.setIsFinalSharedTrack(request.getIsFinalSharedTrack());
        track.setCreatedBy(request.getCreatedBy());

        return mapToResponse(trackRepository.save(track));
    }

    @Override
    public void deleteTrack(UUID id) {
        trackRepository.delete(findTrackById(id));
    }

    private Track findTrackById(UUID id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Round track not found with id: " + id));
    }

    private void validateRoundExists(UUID roundId, UUID eventId) {
        if (!trackRepository.existsRoundInEvent(roundId, eventId)) {
            throw new ResourceNotFoundException("Round not found with id: " + roundId + " in event: " + eventId);
        }
    }

    private void validateDuplicateTrack(RoundTrackRequest request) {
        if (trackRepository.existsByRoundIdAndName(request.getRoundId(), request.getName())) {
            throw new IllegalArgumentException("Round track name already exists in this round");
        }
    }

    private void validateDuplicateTrackForUpdate(UUID id, RoundTrackRequest request) {
        if (trackRepository.existsByRoundIdAndNameAndIdNot(request.getRoundId(), request.getName(), id)) {
            throw new IllegalArgumentException("Round track name already exists in this round");
        }
    }

    private void validateTrackRules(RoundTrackRequest request) {
        if (request.getMaxTeams() != null && request.getMaxTeams() <= 0) {
            throw new IllegalArgumentException("Max teams must be greater than 0 when provided");
        }
        if (request.getTopNToPromote() != null && request.getTopNToPromote() < 0) {
            throw new IllegalArgumentException("Top N to promote must be greater than or equal to 0");
        }
        if (request.getDisplayOrder() != null && request.getDisplayOrder() <= 0) {
            throw new IllegalArgumentException("Display order must be greater than 0");
        }
    }

    private RoundTrackResponse mapToResponse(Track track) {
        return RoundTrackResponse.builder()
                .id(track.getId())
                .eventId(track.getEventId())
                .roundId(track.getRoundId())
                .name(track.getName())
                .challengeTitle(track.getChallengeTitle())
                .challengeDescription(track.getChallengeDescription())
                .challengeFileUrl(track.getChallengeFileUrl())
                .maxTeams(track.getMaxTeams())
                .topNToPromote(track.getTopNToPromote())
                .displayOrder(track.getDisplayOrder())
                .isFinalSharedTrack(track.getIsFinalSharedTrack())
                .createdBy(track.getCreatedBy())
                .createdAt(track.getCreatedAt())
                .updatedAt(track.getUpdatedAt())
                .build();
    }
}
