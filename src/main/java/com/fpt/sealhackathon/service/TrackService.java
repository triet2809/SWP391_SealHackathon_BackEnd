package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.roundtrack.RoundTrackRequest;
import com.fpt.sealhackathon.dto.roundtrack.RoundTrackResponse;

import java.util.List;
import java.util.UUID;

public interface TrackService {

    RoundTrackResponse createTrack(RoundTrackRequest request);

    List<RoundTrackResponse> getAllTracks();

    RoundTrackResponse getTrackById(UUID id);

    List<RoundTrackResponse> getTracksByRoundId(UUID roundId);

    RoundTrackResponse updateTrack(UUID id, RoundTrackRequest request);

    void deleteTrack(UUID id);
}
