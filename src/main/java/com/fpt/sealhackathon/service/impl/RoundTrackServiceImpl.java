package com.fpt.sealhackathon.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackRequest;
import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackResponse;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.Round;
import com.fpt.sealhackathon.entity.RoundTrack;
import com.fpt.sealhackathon.exception.ConflictException;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.RoundTrackMapper;
import com.fpt.sealhackathon.repository.EventRepository;
import com.fpt.sealhackathon.repository.RoundRepository;
import com.fpt.sealhackathon.repository.RoundTrackRepository;
import com.fpt.sealhackathon.service.RoundTrackService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoundTrackServiceImpl implements RoundTrackService {

    private final RoundTrackRepository roundTrackRepository;
    private final RoundRepository roundRepository;
    private final EventRepository eventRepository;
    private final RoundTrackMapper roundTrackMapper;

    @Override
    public RoundTrackResponse create(RoundTrackRequest request) {

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Round round = roundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        // Kiểm tra Round có thuộc Event không
        if (!round.getEvent().getId().equals(event.getId())) {
            throw new ConflictException("Round does not belong to the selected event");
        }

        RoundTrack roundTrack = roundTrackMapper.toEntity(request);
        roundTrack.setEvent(event);
        roundTrack.setRound(round);

        return roundTrackMapper.toResponse(
                roundTrackRepository.save(roundTrack));
    }

    @Override
    public RoundTrackResponse update(UUID id, RoundTrackRequest request) {

        RoundTrack roundTrack = roundTrackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Round track not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Round round = roundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        if (!round.getEvent().getId().equals(event.getId())) {
            throw new ConflictException("Round does not belong to the selected event");
        }

        roundTrackMapper.updateEntity(request, roundTrack);

        roundTrack.setEvent(event);
        roundTrack.setRound(round);

        return roundTrackMapper.toResponse(
                roundTrackRepository.save(roundTrack));
    }

    @Override
    public void delete(UUID id) {

        RoundTrack roundTrack = roundTrackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Round track not found"));

        roundTrackRepository.delete(roundTrack);
    }

    @Override
    public List<RoundTrackResponse> roundTrackFilter(
            UUID eventId,
            UUID roundId,
            String keyword) {

        return roundTrackMapper.toResponseList(
                roundTrackRepository.roundTrackFilter(
                        eventId,
                        roundId,
                        keyword));
    }
}
