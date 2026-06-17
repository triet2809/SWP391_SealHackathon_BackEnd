package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.round.RoundRequest;
import com.fpt.sealhackathon.dto.round.RoundResponse;

import java.util.List;
import java.util.UUID;

public interface RoundService {

    RoundResponse createRound(RoundRequest request);

    List<RoundResponse> getAllRounds();

    RoundResponse getRoundById(UUID id);

    List<RoundResponse> getRoundsByTrackId(UUID trackId);

    RoundResponse updateRound(UUID id, RoundRequest request);

    void deleteRound(UUID id);
}