package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.event.EventRequest;
import com.fpt.sealhackathon.dto.event.EventResponse;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.enums.EventStatus;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.repository.EventRepository;
import com.fpt.sealhackathon.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private static final Set<String> VALID_STATUSES = Set.of(
            "draft",
            "published",
            "registration_open",
            "registration_closed",
            "ongoing",
            "completed",
            "cancelled"
    );

    @Override
    public EventResponse createEvent(EventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(normalizeStatus(request.getStatus()))
                .build();

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EventResponse getEventById(UUID id) {
        Event event = findEventById(id);
        return mapToResponse(event);
    }

    @Override
    public EventResponse updateEvent(UUID id, EventRequest request) {
        Event event = findEventById(id);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            event.setStatus(normalizeStatus(request.getStatus()));
        }

        Event updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    @Override
    public void cancelEvent(UUID id) {
        Event event = findEventById(id);
        event.setStatus(EventStatus.cancelled);
        eventRepository.save(event);
    }

    private Event findEventById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    private EventStatus normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return EventStatus.draft;
        }

        String normalizedStatus = status.toLowerCase();

        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid event status: " + status);
        }

        return EventStatus.valueOf(normalizedStatus);
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .status(event.getStatus().name())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
