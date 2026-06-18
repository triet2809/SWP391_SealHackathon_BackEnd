package com.fpt.sealhackathon.service.impl;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.event.request.TrackRegisterRequest;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.Track;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.repository.EventRepository;
import com.fpt.sealhackathon.repository.TrackRepository;
import com.fpt.sealhackathon.service.TrackService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TrackServiceImpl implements TrackService {
    private final TrackRepository trackRepository;
    private final EventRepository eventRepository; 

    @Override
    public Track registeTrack(TrackRegisterRequest request) {
        Event event = eventRepository.findById(request.getEventId())
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        Track track = Track.builder()
                            .event(event).name(request.getName())
                            .description(request.getDescription())
                            .build();
        return trackRepository.save(track);
    }
    
}
