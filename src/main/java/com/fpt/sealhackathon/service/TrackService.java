package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.track.TrackRequest;
import com.fpt.sealhackathon.dto.track.TrackResponse;

import java.util.List;
import java.util.UUID;

public interface TrackService {

    TrackResponse createTrack(TrackRequest request);

    List<TrackResponse> getAllTracks();

    TrackResponse getTrackById(UUID id);

    TrackResponse updateTrack(UUID id, TrackRequest request);

    void deleteTrack(UUID id);
}
