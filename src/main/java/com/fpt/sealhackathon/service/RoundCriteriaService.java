package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.roundcriteria.RoundCriteriaRequest;
import com.fpt.sealhackathon.dto.roundcriteria.RoundCriteriaResponse;

import java.util.List;
import java.util.UUID;

public interface RoundCriteriaService {

    RoundCriteriaResponse createRoundCriteria(RoundCriteriaRequest request);

    List<RoundCriteriaResponse> getAllRoundCriteria();

    RoundCriteriaResponse getRoundCriteriaById(UUID id);

    List<RoundCriteriaResponse> getRoundCriteriaByRoundId(UUID roundId);

    RoundCriteriaResponse updateRoundCriteria(UUID id, RoundCriteriaRequest request);

    void deleteRoundCriteria(UUID id);
}
