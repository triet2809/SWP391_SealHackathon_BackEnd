package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.round.RoundRequest;
import com.fpt.sealhackathon.dto.round.RoundResponse;
import com.fpt.sealhackathon.entity.Round;
import com.fpt.sealhackathon.entity.enums.RoundStatus;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.repository.RoundRepository;
import com.fpt.sealhackathon.service.RoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {

    private final RoundRepository roundRepository;
    private static final Set<String> VALID_STATUSES = Set.of(
            "draft",
            "open",
            "submission_closed",
            "scoring",
            "ranking_published",
            "completed",
            "cancelled"
    );

    @Override
    public RoundResponse createRound(RoundRequest request) {
        validateEventExists(request.getEventId());
        validateDuplicateRound(request);
        validateRoundRules(request);

        Round round = Round.builder()
                .eventId(request.getEventId())
                .name(request.getName())
                .description(request.getDescription())
                .sequenceNumber(request.getSequenceNumber())
                .status(normalizeStatus(request.getStatus()))
                .startAt(request.getStartAt())
                .submissionDeadline(request.getSubmissionDeadline())
                .scoringDeadline(request.getScoringDeadline())
                .createdBy(request.getCreatedBy())
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
        validateRoundRules(request);

        round.setEventId(request.getEventId());
        round.setName(request.getName());
        round.setDescription(request.getDescription());
        round.setSequenceNumber(request.getSequenceNumber());
        round.setStatus(normalizeStatus(request.getStatus()));
        round.setStartAt(request.getStartAt());
        round.setSubmissionDeadline(request.getSubmissionDeadline());
        round.setScoringDeadline(request.getScoringDeadline());
        round.setCreatedBy(request.getCreatedBy());

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
        if (roundRepository.existsByEventIdAndSequenceNumber(request.getEventId(), request.getSequenceNumber())) {
            throw new IllegalArgumentException("Round sequence number already exists in this event");
        }
    }

    private void validateDuplicateRoundForUpdate(UUID id, RoundRequest request) {
        if (roundRepository.existsByEventIdAndSequenceNumberAndIdNot(
                request.getEventId(),
                request.getSequenceNumber(),
                id
        )) {
            throw new IllegalArgumentException("Round sequence number already exists in this event");
        }
    }

    private RoundStatus normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return RoundStatus.draft;
        }

        String normalizedStatus = status.toLowerCase();
        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid round status: " + status);
        }

        return RoundStatus.valueOf(normalizedStatus);
    }

    private void validateRoundRules(RoundRequest request) {
        if (request.getStartAt() != null
                && request.getSubmissionDeadline() != null
                && !request.getStartAt().isBefore(request.getSubmissionDeadline())) {
            throw new IllegalArgumentException("Round start must be before submission deadline");
        }

        if (request.getSubmissionDeadline() != null
                && request.getScoringDeadline() != null
                && request.getSubmissionDeadline().isAfter(request.getScoringDeadline())) {
            throw new IllegalArgumentException("Submission deadline must be before or equal to scoring deadline");
        }
    }

    private RoundResponse mapToResponse(Round round) {
        return RoundResponse.builder()
                .id(round.getId())
                .eventId(round.getEventId())
                .name(round.getName())
                .description(round.getDescription())
                .sequenceNumber(round.getSequenceNumber())
                .startAt(round.getStartAt())
                .submissionDeadline(round.getSubmissionDeadline())
                .scoringDeadline(round.getScoringDeadline())
                .status(round.getStatus().name())
                .createdBy(round.getCreatedBy())
                .createdAt(round.getCreatedAt())
                .updatedAt(round.getUpdatedAt())
                .build();
    }
}
