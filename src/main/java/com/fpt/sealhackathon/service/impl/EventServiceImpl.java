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
        validateSeasonUniqueness(request, null);
        validateEventRules(request);

        Event event = Event.builder()
                .title(request.getTitle())
                .seasonName(request.getSeasonName())
                .seasonYear(request.getSeasonYear())
                .description(request.getDescription())
                .status(normalizeStatus(request.getStatus()))
                .registrationStartAt(request.getRegistrationStartAt())
                .registrationEndAt(request.getRegistrationEndAt())
                .registrationClosedAt(request.getRegistrationClosedAt())
                .minTeamSize(request.getMinTeamSize())
                .maxTeamSize(request.getMaxTeamSize())
                .createdBy(request.getCreatedBy())
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
        validateSeasonUniqueness(request, id);
        validateEventRules(request);

        event.setTitle(request.getTitle());
        event.setSeasonName(request.getSeasonName());
        event.setSeasonYear(request.getSeasonYear());
        event.setDescription(request.getDescription());
        event.setRegistrationStartAt(request.getRegistrationStartAt());
        event.setRegistrationEndAt(request.getRegistrationEndAt());
        event.setRegistrationClosedAt(request.getRegistrationClosedAt());
        event.setMinTeamSize(request.getMinTeamSize());
        event.setMaxTeamSize(request.getMaxTeamSize());
        event.setCreatedBy(request.getCreatedBy());

        event.setStatus(normalizeStatus(request.getStatus()));

        Event updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    @Override
    public void deleteEvent(UUID id) {
        eventRepository.delete(findEventById(id));
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

    private void validateSeasonUniqueness(EventRequest request, UUID excludedId) {
        if (request.getSeasonName() == null || request.getSeasonYear() == null) {
            return;
        }

        boolean exists = excludedId == null
                ? eventRepository.existsBySeasonNameAndSeasonYear(request.getSeasonName(), request.getSeasonYear())
                : eventRepository.existsBySeasonNameAndSeasonYearAndIdNot(
                request.getSeasonName(),
                request.getSeasonYear(),
                excludedId
        );

        if (exists) {
            throw new IllegalArgumentException("Season name and season year combination already exists");
        }
    }

    private void validateEventRules(EventRequest request) {
        if (request.getRegistrationStartAt() != null
                && request.getRegistrationEndAt() != null
                && !request.getRegistrationStartAt().isBefore(request.getRegistrationEndAt())) {
            throw new IllegalArgumentException("Registration start must be before registration end");
        }

        int minTeamSize = request.getMinTeamSize() == null ? 3 : request.getMinTeamSize();
        int maxTeamSize = request.getMaxTeamSize() == null ? 5 : request.getMaxTeamSize();
        if (maxTeamSize < minTeamSize) {
            throw new IllegalArgumentException("Max team size must be greater than or equal to min team size");
        }
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .seasonName(event.getSeasonName())
                .seasonYear(event.getSeasonYear())
                .description(event.getDescription())
                .status(event.getStatus().name())
                .registrationStartAt(event.getRegistrationStartAt())
                .registrationEndAt(event.getRegistrationEndAt())
                .registrationClosedAt(event.getRegistrationClosedAt())
                .minTeamSize(event.getMinTeamSize())
                .maxTeamSize(event.getMaxTeamSize())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
