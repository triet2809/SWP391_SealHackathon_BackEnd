package com.fpt.sealhackathon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackRequest;
import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackResponse;

@Service
public interface RoundTrackService {

    RoundTrackResponse create(RoundTrackRequest request);

    RoundTrackResponse update(UUID id, RoundTrackRequest request);

    void delete(UUID id);

    List<RoundTrackResponse> roundTrackFilter(
            UUID eventId,
            UUID roundId,
            String keyword);
}
