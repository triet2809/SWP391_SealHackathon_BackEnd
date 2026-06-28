package com.fpt.sealhackathon.service;

import java.util.List;
import java.util.UUID;

import com.fpt.sealhackathon.dto.event.EventRequest;
import com.fpt.sealhackathon.dto.event.EventResponse;
import com.fpt.sealhackathon.entity.enums.EventStatus;

public interface EventService {

    EventResponse create(EventRequest request);

    EventResponse update(UUID id, EventRequest request);

    void delete(UUID id);

    List<EventResponse> eventFilter(String keyword, Integer seasonYear);

    EventResponse changeStatus(UUID id, EventStatus status);
}
