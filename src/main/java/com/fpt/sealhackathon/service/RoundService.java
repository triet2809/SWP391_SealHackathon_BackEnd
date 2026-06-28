package com.fpt.sealhackathon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.round.RoundRequest;
import com.fpt.sealhackathon.dto.round.RoundResponse;
import com.fpt.sealhackathon.entity.enums.RoundStatus;

@Service
public interface RoundService {

    RoundResponse create(RoundRequest request);

    RoundResponse update(UUID id, RoundRequest request);

    List<RoundResponse> roundFilter(UUID eventId, String keyword);

    RoundResponse changeStatus(UUID id, RoundStatus status);
}
