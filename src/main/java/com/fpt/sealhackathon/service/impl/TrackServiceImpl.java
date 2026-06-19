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
        validateEventExists(request.getEventId());
        validateDuplicateTrack(request);

        Track track = Track.builder()
                .eventId(request.getEventId())
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

        validateEventExists(request.getEventId());
        validateDuplicateTrackForUpdate(id, request);

        track.setEventId(request.getEventId());
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

    private void validateEventExists(UUID eventId) {
        if (!trackRepository.existsEventById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
    }

    private void validateDuplicateTrack(TrackRequest request) {
        if (trackRepository.existsByEventIdAndNameIgnoreCase(request.getEventId(), request.getName())) {
            throw new IllegalArgumentException("Track name already exists in this event");
        }
    }

    private void validateDuplicateTrackForUpdate(UUID id, TrackRequest request) {
        if (trackRepository.existsByEventIdAndNameIgnoreCaseAndIdNot(
                request.getEventId(),
                request.getName(),
                id
        )) {
            throw new IllegalArgumentException("Track name already exists in this event");
        }
    }

    private TrackResponse mapToResponse(Track track) {
        return TrackResponse.builder()
                .id(track.getId())
                .eventId(track.getEventId())
                .name(track.getName())
                .description(track.getDescription())
                .createdAt(track.getCreatedAt())
                .updatedAt(track.getUpdatedAt())
                .build();
    }
}
