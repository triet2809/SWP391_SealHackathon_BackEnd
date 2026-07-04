package vn.edu.fpt.seal.modules.track.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.EventStatus;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.event.entity.Event;
import vn.edu.fpt.seal.modules.event.repository.EventRepository;
import vn.edu.fpt.seal.modules.track.dto.CreateTrackRequest;
import vn.edu.fpt.seal.modules.track.dto.TrackResponse;
import vn.edu.fpt.seal.modules.track.dto.UpdateTrackRequest;
import vn.edu.fpt.seal.modules.track.entity.Track;
import vn.edu.fpt.seal.modules.track.mapper.TrackMapper;
import vn.edu.fpt.seal.modules.track.repository.TrackRepository;
import vn.edu.fpt.seal.modules.team.repository.TeamRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public Page<TrackResponse> listByEvent(UUID eventId, Pageable pageable) {
        if (eventId == null) {
            return trackRepository.findAll(pageable).map(this::toResponseWithCount);
        }
        if (!eventRepository.existsById(eventId)) {
            throw ApiException.notFound("Event not found: " + eventId);
        }
        return trackRepository.findByEventId(eventId, pageable).map(this::toResponseWithCount);
    }

    @Transactional(readOnly = true)
    public TrackResponse get(UUID id) {
        return toResponseWithCount(findOrThrow(id));
    }

    private TrackResponse toResponseWithCount(Track t) {
        return TrackMapper.toResponse(t, teamRepository.countByTrackId(t.getId()));
    }

    @Transactional
    public TrackResponse create(CreateTrackRequest req) {
        Event event = eventRepository.findById(req.eventId())
                .orElseThrow(() -> ApiException.notFound("Event not found: " + req.eventId()));

        if (event.getStatus() == EventStatus.completed || event.getStatus() == EventStatus.cancelled) {
            throw ApiException.badRequest("Cannot add tracks to event in status " + event.getStatus());
        }

        String name = req.name().trim();
        if (trackRepository.existsByEventIdAndNameIgnoreCase(event.getId(), name)) {
            throw ApiException.conflict("Track name already exists in this event");
        }

        Track t = Track.builder()
                .event(event)
                .name(name)
                .description(req.description())
                .maxTeams(req.maxTeams())
                .build();
        t = trackRepository.save(t);
        log.info("Track created: id={}, event={}, name={}, maxTeams={}", t.getId(), event.getId(), name, req.maxTeams());
        return toResponseWithCount(t);
    }

    @Transactional
    public TrackResponse update(UUID id, UpdateTrackRequest req) {
        Track t = findOrThrow(id);
        EventStatus es = t.getEvent().getStatus();
        if (es == EventStatus.completed || es == EventStatus.cancelled) {
            throw ApiException.badRequest("Cannot edit track in event status " + es);
        }
        if (req.name() != null) {
            String name = req.name().trim();
            if (!name.equalsIgnoreCase(t.getName())
                    && trackRepository.existsByEventIdAndNameIgnoreCase(t.getEvent().getId(), name)) {
                throw ApiException.conflict("Track name already exists in this event");
            }
            t.setName(name);
        }
        if (req.description() != null) {
            t.setDescription(req.description());
        }
        if (req.maxTeams() != null) {
            // Reject a cap below the number of teams already registered.
            long current = teamRepository.countByTrackId(t.getId());
            if (req.maxTeams() < current) {
                throw ApiException.badRequest("maxTeams (" + req.maxTeams() + ") cannot be less than the " + current + " team(s) already in this track");
            }
            t.setMaxTeams(req.maxTeams());
        }
        return toResponseWithCount(t);
    }

    @Transactional
    public void delete(UUID id) {
        Track t = findOrThrow(id);
        EventStatus es = t.getEvent().getStatus();
        if (es != EventStatus.draft) {
            throw ApiException.badRequest("Tracks can only be deleted while event is draft (current: " + es + ")");
        }
        trackRepository.delete(t);
        log.info("Track deleted: id={}", id);
    }

    private Track findOrThrow(UUID id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Track not found: " + id));
    }
}
