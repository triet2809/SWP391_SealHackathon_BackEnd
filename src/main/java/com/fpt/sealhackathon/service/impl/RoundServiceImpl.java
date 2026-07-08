package com.fpt.sealhackathon.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.round.RoundRequest;
import com.fpt.sealhackathon.dto.round.RoundResponse;
import com.fpt.sealhackathon.dto.round.RoundUpsertRequest;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.Round;
import com.fpt.sealhackathon.entity.enums.RoundStatus;
import com.fpt.sealhackathon.exception.ConflictException;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.RoundMapper;
import com.fpt.sealhackathon.repository.EventRepository;
import com.fpt.sealhackathon.repository.RoundRepository;
import com.fpt.sealhackathon.service.RoundService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoundServiceImpl implements RoundService {

    private final RoundRepository roundRepository;
    private final EventRepository eventRepository;
    private final RoundMapper roundMapper;

    @Override
    public RoundResponse create(UUID id, RoundRequest request) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (roundRepository.existsByEvent_IdAndSequenceNumber(event.getId(), request.getSequenceNumber())) {
            throw new ConflictException(
                    "Sequence number already exists in this event.");
        }

        Round round = roundMapper.toEntity(request);
        round.setEvent(event);
        round.setStatus(RoundStatus.draft);

        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public RoundResponse update(UUID id, RoundUpsertRequest request) {

        Round round = roundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!round.getEvent().getId().equals(request.getEventId())) {
            // rule này do database dump đặt ra
            throw new ConflictException("Cannot change event of an existing round");
        }

        if (roundRepository.existsByEvent_IdAndSequenceNumberAndIdNot(
                event.getId(),
                request.getSequenceNumber(),
                round.getId())) {

            throw new ConflictException(
                    "Sequence number already exists in this event.");
        }

        roundMapper.updateEntityFromRequest(request, round);
        round.setEvent(event);

        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public List<RoundResponse> roundFilter(UUID eventId, String keyword) {

        List<Round> rounds = roundRepository.roundFilter(eventId, keyword);

        return roundMapper.toResponseList(rounds);
    }

    @Override
    public RoundResponse changeStatus(UUID id, RoundStatus status) {

        Round round = roundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        validateStatusTransition(round.getStatus(), status);

        round.setStatus(status);

        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public List<RoundResponse> roundByEventId(UUID eventId) {
        List<Round> rounds = roundRepository.findByEvent_Id(eventId);

        return roundMapper.toResponseList(rounds);
    }

    @Override
    public RoundResponse rounndById(UUID id) {
        Round round = roundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        return roundMapper.toResponse(round);
    }

    @Override
    @Transactional
    public void delete(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        try {
            roundRepository.delete(round);
            roundRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Cannot delete round because using.");
        }
    }

    private void validateStatusTransition(RoundStatus current, RoundStatus next) {

        boolean valid = switch (current) {

            case draft ->
                next == RoundStatus.open;

            case open ->
                next == RoundStatus.submission_closed;

            case submission_closed ->
                next == RoundStatus.scoring;

            case scoring ->
                next == RoundStatus.ranking_published;

            case ranking_published ->
                next == RoundStatus.completed;

            case completed ->
                false;

            case cancelled ->
                false;
        };

        if (!valid) {
            throw new ConflictException(
                    "Invalid round status transition: " + current + " -> " + next);
        }
    }
}
