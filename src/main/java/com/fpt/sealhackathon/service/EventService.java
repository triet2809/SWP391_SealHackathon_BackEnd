package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.event.EventRequest;
import com.fpt.sealhackathon.dto.event.EventResponse;

import java.util.List;
import java.util.UUID;

public interface EventService {

    EventResponse createEvent(EventRequest request);

    List<EventResponse> getAllEvents();

    EventResponse getEventById(UUID id);

    EventResponse updateEvent(UUID id, EventRequest request);

    void cancelEvent(UUID id);
}
