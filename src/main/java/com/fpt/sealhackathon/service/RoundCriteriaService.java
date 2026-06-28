package com.fpt.sealhackathon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.RoundCriteria.RoundCriteriaRequest;
import com.fpt.sealhackathon.dto.RoundCriteria.RoundCriteriaResponse;

@Service
public interface RoundCriteriaService {

    RoundCriteriaResponse create(RoundCriteriaRequest request);

    RoundCriteriaResponse update(UUID id, RoundCriteriaRequest request);

    void delete(UUID id);

    List<RoundCriteriaResponse> filter(
            UUID eventId,
            UUID roundId,
            UUID roundTrackId,
            String keyword);
}
