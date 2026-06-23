package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.round.RoundRequest;
import com.fpt.sealhackathon.dto.round.RoundResponse;
import com.fpt.sealhackathon.entity.Round;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.repository.RoundRepository;
import com.fpt.sealhackathon.service.RoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {

    private final RoundRepository roundRepository;

    @Override
    public RoundResponse createRound(RoundRequest request) {
        validateEventExists(request.getEventId());
        validateDuplicateRound(request);

        Round round = Round.builder()
                .eventId(request.getEventId())
                .name(request.getName())
                .sequenceNumber(request.getSequenceNumber())
                .submissionDeadline(request.getSubmissionDeadline())
                .build();

        Round savedRound = roundRepository.save(round);
        return mapToResponse(savedRound);
    }

    @Override
    public List<RoundResponse> getAllRounds() {
        return roundRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RoundResponse getRoundById(UUID id) {
        Round round = findRoundById(id);
        return mapToResponse(round);
    }

    @Override
    public List<RoundResponse> getRoundsByEventId(UUID eventId) {
        validateEventExists(eventId);

        return roundRepository.findByEventIdOrderBySequenceNumberAsc(eventId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RoundResponse updateRound(UUID id, RoundRequest request) {
        Round round = findRoundById(id);

        validateEventExists(request.getEventId());
        validateDuplicateRoundForUpdate(id, request);

        round.setEventId(request.getEventId());
        round.setName(request.getName());
        round.setSequenceNumber(request.getSequenceNumber());
        round.setSubmissionDeadline(request.getSubmissionDeadline());

        Round updatedRound = roundRepository.save(round);
        return mapToResponse(updatedRound);
    }

    @Override
    public void deleteRound(UUID id) {
        Round round = findRoundById(id);
        roundRepository.delete(round);
    }

    private Round findRoundById(UUID id) {
        return roundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found with id: " + id));
    }

    private void validateEventExists(UUID eventId) {
        if (!roundRepository.existsEventById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
    }

    private void validateDuplicateRound(RoundRequest request) {
        if (roundRepository.existsByEventIdAndNameIgnoreCase(request.getEventId(), request.getName())) {
            throw new IllegalArgumentException("Round name already exists in this event");
        }

        if (roundRepository.existsByEventIdAndSequenceNumber(request.getEventId(), request.getSequenceNumber())) {
            throw new IllegalArgumentException("Round sequence number already exists in this event");
        }
    }

    private void validateDuplicateRoundForUpdate(UUID id, RoundRequest request) {
        if (roundRepository.existsByEventIdAndNameIgnoreCaseAndIdNot(
                request.getEventId(),
                request.getName(),
                id
        )) {
            throw new IllegalArgumentException("Round name already exists in this event");
        }

        if (roundRepository.existsByEventIdAndSequenceNumberAndIdNot(
                request.getEventId(),
                request.getSequenceNumber(),
                id
        )) {
            throw new IllegalArgumentException("Round sequence number already exists in this event");
        }
    }

    private RoundResponse mapToResponse(Round round) {
        return RoundResponse.builder()
                .id(round.getId())
                .eventId(round.getEventId())
                .name(round.getName())
                .sequenceNumber(round.getSequenceNumber())
                .submissionDeadline(round.getSubmissionDeadline())
                .status(round.getStatus().name())
                .createdAt(round.getCreatedAt())
                .updatedAt(round.getUpdatedAt())
                .build();
    }
}
