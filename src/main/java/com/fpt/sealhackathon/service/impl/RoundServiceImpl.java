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
        validateTrackExists(request.getTrackId());
        validateDuplicateRound(request);

        Round round = Round.builder()
                .trackId(request.getTrackId())
                .name(request.getName())
                .sequenceNumber(request.getSequenceNumber())
                .submissionDeadline(request.getSubmissionDeadline())
                .topNToPromote(request.getTopNToPromote())
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
    public List<RoundResponse> getRoundsByTrackId(UUID trackId) {
        validateTrackExists(trackId);

        return roundRepository.findByTrackIdOrderBySequenceNumberAsc(trackId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RoundResponse updateRound(UUID id, RoundRequest request) {
        Round round = findRoundById(id);

        validateTrackExists(request.getTrackId());
        validateDuplicateRoundForUpdate(id, request);

        round.setTrackId(request.getTrackId());
        round.setName(request.getName());
        round.setSequenceNumber(request.getSequenceNumber());
        round.setSubmissionDeadline(request.getSubmissionDeadline());
        round.setTopNToPromote(request.getTopNToPromote());

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

    private void validateTrackExists(UUID trackId) {
        if (!roundRepository.existsTrackById(trackId)) {
            throw new ResourceNotFoundException("Track not found with id: " + trackId);
        }
    }

    private void validateDuplicateRound(RoundRequest request) {
        if (roundRepository.existsByTrackIdAndNameIgnoreCase(request.getTrackId(), request.getName())) {
            throw new IllegalArgumentException("Round name already exists in this track");
        }

        if (roundRepository.existsByTrackIdAndSequenceNumber(request.getTrackId(), request.getSequenceNumber())) {
            throw new IllegalArgumentException("Round sequence number already exists in this track");
        }
    }

    private void validateDuplicateRoundForUpdate(UUID id, RoundRequest request) {
        if (roundRepository.existsByTrackIdAndNameIgnoreCaseAndIdNot(
                request.getTrackId(),
                request.getName(),
                id
        )) {
            throw new IllegalArgumentException("Round name already exists in this track");
        }

        if (roundRepository.existsByTrackIdAndSequenceNumberAndIdNot(
                request.getTrackId(),
                request.getSequenceNumber(),
                id
        )) {
            throw new IllegalArgumentException("Round sequence number already exists in this track");
        }
    }

    private RoundResponse mapToResponse(Round round) {
        return RoundResponse.builder()
                .id(round.getId())
                .trackId(round.getTrackId())
                .name(round.getName())
                .sequenceNumber(round.getSequenceNumber())
                .submissionDeadline(round.getSubmissionDeadline())
                .topNToPromote(round.getTopNToPromote())
                .createdAt(round.getCreatedAt())
                .updatedAt(round.getUpdatedAt())
                .build();
    }
}