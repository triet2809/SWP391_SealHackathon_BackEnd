package com.fpt.sealhackathon.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
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
        public RoundTrackResponse create(UUID id, RoundTrackRequest request) {

                Event event = eventRepository.findById(request.getEventId())
                                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                Round round = roundRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Round not found"));

                // Kiểm tra Round có thuộc Event không
                if (!round.getEvent().getId().equals(event.getId())) {
                        throw new ConflictException("Round does not belong to the selected event");
                }

                validatePromotionRule(
                                request.getMaxTeams(),
                                request.getTopNToPromote());

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

                validatePromotionRule(
                                request.getMaxTeams(),
                                request.getTopNToPromote());

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

                try {
                        roundTrackRepository.delete(roundTrack);
                        roundTrackRepository.flush();
                } catch (DataIntegrityViolationException ex) {
                        throw new ConflictException(
                                        "Cannot delete round track because using.");
                }
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

        @Override
        public List<RoundTrackResponse> roundTrackByTrack(UUID roundId) {
                return roundTrackMapper.toResponseList(
                                roundTrackRepository.findByRound_Id(roundId));
        }

        @Override
        public RoundTrackResponse roundTrackById(UUID id) {
                RoundTrack roundTrack = roundTrackRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Round track not found"));

                return roundTrackMapper.toResponse(roundTrack);
        }

        @Override
        public RoundTrackResponse updatePromotionRule(UUID id, Integer topNToPromote) {
                RoundTrack roundTrack = roundTrackRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Round track not found"));

                validatePromotionRule(roundTrack.getMaxTeams(), topNToPromote);

                roundTrack.setTopNToPromote(topNToPromote);

                return roundTrackMapper.toResponse(
                                roundTrackRepository.save(roundTrack));
        }

        private void validatePromotionRule(Integer maxTeams, Integer topNToPromote) {

                if (maxTeams != null && topNToPromote > maxTeams) {
                        throw new ConflictException(
                                        "Top N to promote must not be greater than max teams.");
                }
        }
}
