package com.fpt.sealhackathon.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.event.EventRequest;
import com.fpt.sealhackathon.dto.event.EventResponse;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.enums.EventStatus;
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

        Event event = eventMapper.toEntity(request);

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse update(UUID id, EventRequest request) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        eventMapper.updateEntity(request, event);

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public void delete(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        eventRepository.delete(event);
    }

    @Override
    public List<EventResponse> eventFilter(String keyword, Integer seasonYear) {

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        return eventMapper.toResponseList(
                eventRepository.eventFilter(keyword, seasonYear));
    }

    @Override
    public EventResponse changeStatus(UUID id, EventStatus status) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        event.setStatus(status);
        event.setUpdatedAt(LocalDateTime.now());

        return eventMapper.toResponse(eventRepository.save(event));
    }

}
