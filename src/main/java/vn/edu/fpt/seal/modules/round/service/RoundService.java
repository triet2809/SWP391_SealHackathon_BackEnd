package vn.edu.fpt.seal.modules.round.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.EventStatus;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.round.dto.CreateRoundRequest;
import vn.edu.fpt.seal.modules.round.dto.RoundResponse;
import vn.edu.fpt.seal.modules.round.dto.UpdateRoundRequest;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.round.mapper.RoundMapper;
import vn.edu.fpt.seal.modules.round.repository.RoundRepository;
import vn.edu.fpt.seal.modules.track.entity.Track;
import vn.edu.fpt.seal.modules.track.repository.TrackRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundRepository roundRepository;
    private final TrackRepository trackRepository;

    @Transactional(readOnly = true)
    public Page<RoundResponse> listByTrack(UUID trackId, Pageable pageable) {
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("sequenceNumber").ascending());
        if (trackId == null) {
            return roundRepository.findAll(effectivePageable).map(RoundMapper::toResponse);
        }
        if (!trackRepository.existsById(trackId)) {
            throw ApiException.notFound("Track not found: " + trackId);
        }
        return roundRepository.findByTrackId(trackId, effectivePageable).map(RoundMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RoundResponse get(UUID id) {
        return RoundMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public RoundResponse create(CreateRoundRequest req) {
        Track track = trackRepository.findById(req.trackId())
                .orElseThrow(() -> ApiException.notFound("Track not found: " + req.trackId()));
        ensureEditable(track);

        String name = req.name().trim();
        UUID trackId = track.getId();
        if (roundRepository.existsByTrackIdAndNameIgnoreCase(trackId, name)) {
            throw ApiException.conflict("Round name already exists in this track");
        }
        int sequenceNumber = resolveSequenceNumber(trackId, req.sequenceNumber());

        Round round = Round.builder()
                .track(track)
                .name(name)
                .sequenceNumber(sequenceNumber)
                .submissionDeadline(req.submissionDeadline())
                .topNToPromote(req.topNToPromote())
                .build();
        round = roundRepository.save(round);
        log.info("Round created: id={}, track={}, sequence={}, name={}",
                round.getId(), trackId, round.getSequenceNumber(), round.getName());
        return RoundMapper.toResponse(round);
    }

    @Transactional
    public RoundResponse update(UUID id, UpdateRoundRequest req) {
        Round round = findOrThrow(id);
        ensureEditable(round.getTrack());
        UUID trackId = round.getTrack().getId();

        if (req.name() != null) {
            String name = req.name().trim();
            if (!name.equalsIgnoreCase(round.getName())
                    && roundRepository.existsByTrackIdAndNameIgnoreCase(trackId, name)) {
                throw ApiException.conflict("Round name already exists in this track");
            }
            round.setName(name);
        }
        if (req.sequenceNumber() != null) {
            if (!req.sequenceNumber().equals(round.getSequenceNumber())
                    && roundRepository.existsByTrackIdAndSequenceNumber(trackId, req.sequenceNumber())) {
                throw ApiException.conflict("Round sequence number already exists in this track");
            }
            round.setSequenceNumber(req.sequenceNumber());
        }
        if (req.submissionDeadline() != null) {
            round.setSubmissionDeadline(req.submissionDeadline());
        }
        if (req.topNToPromote() != null) {
            round.setTopNToPromote(req.topNToPromote());
        }
        return RoundMapper.toResponse(round);
    }

    @Transactional
    public void delete(UUID id) {
        Round round = findOrThrow(id);
        ensureDraft(round.getTrack());
        roundRepository.delete(round);
        log.info("Round deleted: id={}", id);
    }

    private int resolveSequenceNumber(UUID trackId, Integer requestedSequenceNumber) {
        if (requestedSequenceNumber != null) {
            if (roundRepository.existsByTrackIdAndSequenceNumber(trackId, requestedSequenceNumber)) {
                throw ApiException.conflict("Round sequence number already exists in this track");
            }
            return requestedSequenceNumber;
        }
        return roundRepository.findTopByTrackIdOrderBySequenceNumberDesc(trackId)
                .map(r -> r.getSequenceNumber() + 1)
                .orElse(1);
    }

    private Round findOrThrow(UUID id) {
        return roundRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Round not found: " + id));
    }

    private void ensureEditable(Track track) {
        EventStatus status = track.getEvent().getStatus();
        if (status == EventStatus.completed || status == EventStatus.cancelled) {
            throw ApiException.badRequest("Cannot edit rounds in event status " + status);
        }
    }

    private void ensureDraft(Track track) {
        EventStatus status = track.getEvent().getStatus();
        if (status != EventStatus.draft) {
            throw ApiException.badRequest("Rounds can only be deleted while event is draft (current: " + status + ")");
        }
    }
}
