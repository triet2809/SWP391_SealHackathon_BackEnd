package vn.edu.fpt.seal.modules.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.EventStatus;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.event.dto.CreateEventRequest;
import vn.edu.fpt.seal.modules.event.dto.EventResponse;
import vn.edu.fpt.seal.modules.event.dto.UpdateEventRequest;
import vn.edu.fpt.seal.modules.event.entity.Event;
import vn.edu.fpt.seal.modules.event.mapper.EventMapper;
import vn.edu.fpt.seal.modules.event.repository.EventRepository;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    // Status transition rules:
    //   draft     -> published, cancelled
    //   published -> ongoing, cancelled
    //   ongoing   -> completed, cancelled
    //   completed -> (terminal)
    //   cancelled -> (terminal)
    private static final Map<EventStatus, Set<EventStatus>> ALLOWED_TRANSITIONS = Map.of(
            EventStatus.draft, EnumSet.of(EventStatus.published, EventStatus.cancelled),
            EventStatus.published, EnumSet.of(EventStatus.ongoing, EventStatus.cancelled),
            EventStatus.ongoing, EnumSet.of(EventStatus.completed, EventStatus.cancelled),
            EventStatus.completed, EnumSet.noneOf(EventStatus.class),
            EventStatus.cancelled, EnumSet.noneOf(EventStatus.class)
    );

    @Transactional(readOnly = true)
    public Page<EventResponse> list(EventStatus status, Pageable pageable) {
        Page<Event> page = (status == null)
                ? eventRepository.findAll(pageable)
                : eventRepository.findByStatus(status, pageable);
        return page.map(EventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EventResponse get(UUID id) {
        return EventMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public EventResponse create(CreateEventRequest req) {
        String title = req.title().trim();
        if (eventRepository.existsByTitleIgnoreCase(title)) {
            throw ApiException.conflict("Event title already exists");
        }
        Event e = Event.builder()
                .title(title)
                .description(req.description())
                .status(EventStatus.draft)
                .build();
        e = eventRepository.save(e);
        log.info("Event created: id={}, title={}", e.getId(), e.getTitle());
        return EventMapper.toResponse(e);
    }

    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest req) {
        Event e = findOrThrow(id);
        if (e.getStatus() == EventStatus.completed || e.getStatus() == EventStatus.cancelled) {
            throw ApiException.badRequest("Cannot edit event in status " + e.getStatus());
        }
        if (req.title() != null) {
            String title = req.title().trim();
            if (!title.equalsIgnoreCase(e.getTitle())
                    && eventRepository.existsByTitleIgnoreCase(title)) {
                throw ApiException.conflict("Event title already exists");
            }
            e.setTitle(title);
        }
        if (req.description() != null) {
            e.setDescription(req.description());
        }
        return EventMapper.toResponse(e);
    }

    @Transactional
    public EventResponse changeStatus(UUID id, EventStatus target) {
        Event e = findOrThrow(id);
        EventStatus current = e.getStatus();
        if (current == target) {
            return EventMapper.toResponse(e);
        }
        Set<EventStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(EventStatus.class));
        if (!allowed.contains(target)) {
            throw ApiException.badRequest(
                    "Invalid status transition: " + current + " -> " + target);
        }
        e.setStatus(target);
        log.info("Event {} status: {} -> {}", e.getId(), current, target);
        return EventMapper.toResponse(e);
    }

    @Transactional
    public void delete(UUID id) {
        Event e = findOrThrow(id);
        if (e.getStatus() != EventStatus.draft) {
            throw ApiException.badRequest("Only draft events can be deleted; cancel instead.");
        }
        eventRepository.delete(e);
        log.info("Event deleted: id={}", id);
    }

    private Event findOrThrow(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Event not found: " + id));
    }
}
