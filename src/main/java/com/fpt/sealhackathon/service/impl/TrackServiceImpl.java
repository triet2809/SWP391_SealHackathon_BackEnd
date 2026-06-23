package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.track.TrackRequest;
import com.fpt.sealhackathon.dto.track.TrackResponse;
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
    public TrackResponse createTrack(TrackRequest request) {
        validateRoundExists(request.getRoundId(), request.getEventId());
        validateDuplicateTrack(request);

        Track track = Track.builder()
                .eventId(request.getEventId())
                .roundId(request.getRoundId())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Track savedTrack = trackRepository.save(track);
        return mapToResponse(savedTrack);
    }

    @Override
    public List<TrackResponse> getAllTracks() {
        return trackRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public TrackResponse getTrackById(UUID id) {
        Track track = findTrackById(id);
        return mapToResponse(track);
    }

    @Override
    public TrackResponse updateTrack(UUID id, TrackRequest request) {
        Track track = findTrackById(id);

        validateRoundExists(request.getRoundId(), request.getEventId());
        validateDuplicateTrackForUpdate(id, request);

        track.setEventId(request.getEventId());
        track.setRoundId(request.getRoundId());
        track.setName(request.getName());
        track.setDescription(request.getDescription());

        Track updatedTrack = trackRepository.save(track);
        return mapToResponse(updatedTrack);
    }

    @Override
    public void deleteTrack(UUID id) {
        Track track = findTrackById(id);
        trackRepository.delete(track);
    }

    private Track findTrackById(UUID id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + id));
    }

    private void validateRoundExists(UUID roundId, UUID eventId) {
        if (!trackRepository.existsRoundInEvent(roundId, eventId)) {
            throw new ResourceNotFoundException(
                    "Round not found with id: " + roundId + " in event: " + eventId
            );
        }
    }

    private void validateDuplicateTrack(TrackRequest request) {
        if (trackRepository.existsByRoundIdAndNameIgnoreCase(request.getRoundId(), request.getName())) {
            throw new IllegalArgumentException("Track name already exists in this round");
        }
    }

    private void validateDuplicateTrackForUpdate(UUID id, TrackRequest request) {
        if (trackRepository.existsByRoundIdAndNameIgnoreCaseAndIdNot(
                request.getRoundId(),
                request.getName(),
                id
        )) {
            throw new IllegalArgumentException("Track name already exists in this round");
        }
    }

    private TrackResponse mapToResponse(Track track) {
        return TrackResponse.builder()
                .id(track.getId())
                .eventId(track.getEventId())
                .roundId(track.getRoundId())
                .name(track.getName())
                .description(track.getDescription())
                .createdAt(track.getCreatedAt())
                .updatedAt(track.getUpdatedAt())
                .build();
    }
}
