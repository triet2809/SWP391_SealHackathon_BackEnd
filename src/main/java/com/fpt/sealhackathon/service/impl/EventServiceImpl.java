package com.fpt.sealhackathon.service.impl;

import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.event.EventRequest;
import com.fpt.sealhackathon.dto.event.EventResponse;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.enums.EventStatus;
import com.fpt.sealhackathon.exception.ConflictException;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.EventMapper;
import com.fpt.sealhackathon.repository.EventRepository;
import com.fpt.sealhackathon.service.EventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponse create(EventRequest request) {

        if (eventRepository.existsBySeasonNameAndSeasonYear(
                request.getSeasonName(),
                request.getSeasonYear())) {

            throw new ConflictException(
                    "Season '" + request.getSeasonName()
                            + "' of year " + request.getSeasonYear()
                            + " already exists");
        }

        Event event = eventMapper.toEntity(request);
        event.setStatus(EventStatus.draft);

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse update(UUID id, EventRequest request) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (eventRepository.existsBySeasonNameAndSeasonYearAndIdNot(
                request.getSeasonName(),
                request.getSeasonYear(),
                id)) {

            throw new ConflictException(
                    "Season '" + request.getSeasonName()
                            + "' of year " + request.getSeasonYear()
                            + " already exists");
        }

        eventMapper.updateEntity(request, event);

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public List<EventResponse> eventFilter(String keyword, Integer seasonYear, EventStatus status) {

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        return eventMapper.toResponseList(
                eventRepository.eventFilter(keyword, seasonYear, status != null ? status.toString() : null));
    }

    @Override
    public EventResponse changeStatus(UUID id, EventStatus status) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        validateStatusTransition(event.getStatus(), status);
        event.setStatus(status);
        event.setUpdatedAt(LocalDateTime.now());

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse eventById(UUID id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return eventMapper.toResponse(event);
    }

    private void validateStatusTransition(EventStatus current, EventStatus next) {

        switch (current) {

            case draft -> {
                if (next != EventStatus.published) {
                    throw new ConflictException(
                            "Event must be published before opening registration");
                }
            }

            case published -> {
                if (next != EventStatus.registration_open) {
                    throw new ConflictException(
                            "Registration can only be opened after publishing");
                }
            }

            case registration_open -> {
                if (next != EventStatus.registration_closed) {
                    throw new ConflictException(
                            "Registration must be closed before starting event");
                }
            }

            case registration_closed -> {
                if (next != EventStatus.ongoing) {
                    throw new ConflictException(
                            "Event can only start after registration is closed");
                }
            }

            case ongoing -> {
                if (next != EventStatus.completed) {
                    throw new ConflictException(
                            "Only an ongoing event can be completed");
                }
            }

            case completed -> {
                throw new ConflictException("Completed event cannot change status");
            }

            case cancelled -> {
                throw new ConflictException("Cancelled event cannot change status");
            }
        }
    }

}
