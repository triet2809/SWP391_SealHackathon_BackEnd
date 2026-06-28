package com.fpt.sealhackathon.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.RoundCriteria.RoundCriteriaRequest;
import com.fpt.sealhackathon.dto.RoundCriteria.RoundCriteriaResponse;
import com.fpt.sealhackathon.entity.CriteriaTemplate;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.Round;
import com.fpt.sealhackathon.entity.RoundCriteria;
import com.fpt.sealhackathon.entity.RoundTrack;
import com.fpt.sealhackathon.exception.ConflictException;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.RoundCriteriaMapper;
import com.fpt.sealhackathon.repository.CriteriaTemplateRepository;
import com.fpt.sealhackathon.repository.EventRepository;
import com.fpt.sealhackathon.repository.RoundCriteriaRepository;
import com.fpt.sealhackathon.repository.RoundRepository;
import com.fpt.sealhackathon.repository.RoundTrackRepository;
import com.fpt.sealhackathon.service.RoundCriteriaService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoundCriteriaServiceImpl implements RoundCriteriaService {

    private final RoundCriteriaRepository repository;
    private final RoundCriteriaMapper mapper;

    private final EventRepository eventRepository;
    private final RoundRepository roundRepository;
    private final RoundTrackRepository roundTrackRepository;
    private final CriteriaTemplateRepository templateRepository;

    @Override
    @Transactional
    public RoundCriteriaResponse create(RoundCriteriaRequest request) {

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Round round = roundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        // CHECK: Round phải thuộc Event
        if (!round.getEvent().getId().equals(event.getId())) {
            throw new ConflictException("Round does not belong to the selected event");
        }

        RoundTrack roundTrack = null;
        if (request.getRoundTrackId() != null) {

            roundTrack = roundTrackRepository.findById(request.getRoundTrackId())
                    .orElseThrow(() -> new ResourceNotFoundException("RoundTrack not found"));

            // CHECK: RoundTrack phải thuộc cùng Event + Round
            if (!roundTrack.getEvent().getId().equals(event.getId())) {
                throw new ConflictException("RoundTrack does not belong to the selected event");
            }

            if (!roundTrack.getRound().getId().equals(round.getId())) {
                throw new ConflictException("RoundTrack does not belong to the selected round");
            }
        }

        CriteriaTemplate template = null;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        }

        RoundCriteria entity = mapper.toEntity(request);

        entity.setEvent(event);
        entity.setRound(round);
        entity.setRoundTrack(roundTrack);
        entity.setTemplate(template);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public RoundCriteriaResponse update(UUID id, RoundCriteriaRequest request) {

        RoundCriteria entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoundCriteria not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Round round = roundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

        if (!round.getEvent().getId().equals(event.getId())) {
            throw new ConflictException("Round does not belong to the selected event");
        }

        RoundTrack roundTrack = null;
        if (request.getRoundTrackId() != null) {

            roundTrack = roundTrackRepository.findById(request.getRoundTrackId())
                    .orElseThrow(() -> new ResourceNotFoundException("RoundTrack not found"));

            if (!roundTrack.getEvent().getId().equals(event.getId())) {
                throw new ConflictException("RoundTrack does not belong to the selected event");
            }

            if (!roundTrack.getRound().getId().equals(round.getId())) {
                throw new ConflictException("RoundTrack does not belong to the selected round");
            }
        }

        CriteriaTemplate template = null;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        }

        mapper.updateEntity(request, entity);

        entity.setEvent(event);
        entity.setRound(round);
        entity.setRoundTrack(roundTrack);
        entity.setTemplate(template);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(UUID id) {

        RoundCriteria entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoundCriteria not found"));

        repository.delete(entity);
    }

    @Override
    public List<RoundCriteriaResponse> filter(
            UUID eventId,
            UUID roundId,
            UUID roundTrackId,
            String keyword) {

        return mapper.toResponseList(
                repository.filter(eventId, roundId, roundTrackId, keyword));
    }
}
