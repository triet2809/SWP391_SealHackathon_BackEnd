package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.roundcriteria.RoundCriteriaRequest;
import com.fpt.sealhackathon.dto.roundcriteria.RoundCriteriaResponse;
import com.fpt.sealhackathon.entity.RoundCriteria;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.repository.RoundCriteriaRepository;
import com.fpt.sealhackathon.service.RoundCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoundCriteriaServiceImpl implements RoundCriteriaService {

    private final RoundCriteriaRepository roundCriteriaRepository;

    @Override
    public RoundCriteriaResponse createRoundCriteria(RoundCriteriaRequest request) {
        validateReferences(request);
        validateUniqueName(request, null);

        RoundCriteria roundCriteria = RoundCriteria.builder()
                .eventId(request.getEventId())
                .roundId(request.getRoundId())
                .roundTrackId(request.getRoundTrackId())
                .templateId(request.getTemplateId())
                .name(request.getName())
                .weight(request.getWeight())
                .description(request.getDescription())
                .build();
        return mapToResponse(roundCriteriaRepository.save(roundCriteria));
    }

    @Override
    public List<RoundCriteriaResponse> getAllRoundCriteria() {
        return roundCriteriaRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public RoundCriteriaResponse getRoundCriteriaById(UUID id) {
        return mapToResponse(findRoundCriteriaById(id));
    }

    @Override
    public List<RoundCriteriaResponse> getRoundCriteriaByRoundId(UUID roundId) {
        return roundCriteriaRepository.findByRoundIdOrderByNameAsc(roundId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public RoundCriteriaResponse updateRoundCriteria(UUID id, RoundCriteriaRequest request) {
        RoundCriteria roundCriteria = findRoundCriteriaById(id);
        validateReferences(request);
        validateUniqueName(request, id);

        roundCriteria.setEventId(request.getEventId());
        roundCriteria.setRoundId(request.getRoundId());
        roundCriteria.setRoundTrackId(request.getRoundTrackId());
        roundCriteria.setTemplateId(request.getTemplateId());
        roundCriteria.setName(request.getName());
        roundCriteria.setWeight(request.getWeight());
        roundCriteria.setDescription(request.getDescription());
        return mapToResponse(roundCriteriaRepository.save(roundCriteria));
    }

    @Override
    public void deleteRoundCriteria(UUID id) {
        roundCriteriaRepository.delete(findRoundCriteriaById(id));
    }

    private RoundCriteria findRoundCriteriaById(UUID id) {
        return roundCriteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Round criteria not found with id: " + id));
    }

    private void validateReferences(RoundCriteriaRequest request) {
        if (!roundCriteriaRepository.existsRoundInEvent(request.getRoundId(), request.getEventId())) {
            throw new ResourceNotFoundException("Round not found with id: " + request.getRoundId() + " in event: " + request.getEventId());
        }
        if (request.getRoundTrackId() != null
                && !roundCriteriaRepository.existsRoundTrackInRoundEvent(request.getRoundTrackId(), request.getRoundId(), request.getEventId())) {
            throw new ResourceNotFoundException("Round track not found with id: " + request.getRoundTrackId() + " in the given round and event");
        }
        if (request.getTemplateId() != null && !roundCriteriaRepository.existsTemplateById(request.getTemplateId())) {
            throw new ResourceNotFoundException("Criteria template not found with id: " + request.getTemplateId());
        }
    }

    private void validateUniqueName(RoundCriteriaRequest request, UUID excludedId) {
        boolean exists;
        if (request.getRoundTrackId() == null) {
            exists = excludedId == null
                    ? roundCriteriaRepository.existsByRoundIdAndRoundTrackIdIsNullAndName(request.getRoundId(), request.getName())
                    : roundCriteriaRepository.existsByRoundIdAndRoundTrackIdIsNullAndNameAndIdNot(request.getRoundId(), request.getName(), excludedId);
        } else {
            exists = excludedId == null
                    ? roundCriteriaRepository.existsByRoundIdAndRoundTrackIdAndName(request.getRoundId(), request.getRoundTrackId(), request.getName())
                    : roundCriteriaRepository.existsByRoundIdAndRoundTrackIdAndNameAndIdNot(
                    request.getRoundId(),
                    request.getRoundTrackId(),
                    request.getName(),
                    excludedId
            );
        }

        if (exists) {
            throw new IllegalArgumentException("Round criteria name already exists in this scope");
        }
    }

    private RoundCriteriaResponse mapToResponse(RoundCriteria roundCriteria) {
        return RoundCriteriaResponse.builder()
                .id(roundCriteria.getId())
                .eventId(roundCriteria.getEventId())
                .roundId(roundCriteria.getRoundId())
                .roundTrackId(roundCriteria.getRoundTrackId())
                .templateId(roundCriteria.getTemplateId())
                .name(roundCriteria.getName())
                .weight(roundCriteria.getWeight())
                .description(roundCriteria.getDescription())
                .createdAt(roundCriteria.getCreatedAt())
                .updatedAt(roundCriteria.getUpdatedAt())
                .build();
    }
}
