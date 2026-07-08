package com.fpt.sealhackathon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fpt.sealhackathon.dto.event.EventRequest;
import com.fpt.sealhackathon.dto.event.EventResponse;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.enums.EventStatus;
import com.fpt.sealhackathon.exception.ConflictException;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.EventMapper;
import com.fpt.sealhackathon.repository.EventRepository;
import com.fpt.sealhackathon.service.EventService;
import com.fpt.sealhackathon.service.impl.EventServiceImpl;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    private EventService eventService;

    private UUID eventId;
    private Event event;
    private EventRequest request;
    private EventResponse response;

    @BeforeEach
    void setUp() {

        eventService = new EventServiceImpl(eventRepository, eventMapper);

        eventId = UUID.randomUUID();

        request = EventRequest.builder()
                .title("Hackathon 2026")
                .seasonName("Summer")
                .seasonYear(2026)
                .description("Hackathon")
                .minTeamSize(3)
                .maxTeamSize(5)
                .build();

        event = Event.builder()
                .id(eventId)
                .title(request.getTitle())
                .seasonName(request.getSeasonName())
                .seasonYear(request.getSeasonYear())
                .description(request.getDescription())
                .status(EventStatus.draft)
                .minTeamSize(request.getMinTeamSize())
                .maxTeamSize(request.getMaxTeamSize())
                .build();

        response = EventResponse.builder()
                .id(eventId)
                .title(request.getTitle())
                .seasonName(request.getSeasonName())
                .seasonYear(request.getSeasonYear())
                .description(request.getDescription())
                .status(EventStatus.draft)
                .minTeamSize(request.getMinTeamSize())
                .maxTeamSize(request.getMaxTeamSize())
                .build();
    }

    @Test
    void changeStatus_DraftToPublished_ShouldSuccess() {

        event.setStatus(EventStatus.draft);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        eventService.changeStatus(eventId, EventStatus.published);

        assertEquals(EventStatus.published, event.getStatus());

        verify(eventRepository).save(event);
    }

    @Test
    void changeStatus_PublishedToRegistrationOpen_ShouldSuccess() {

        event.setStatus(EventStatus.published);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        eventService.changeStatus(eventId, EventStatus.registration_open);

        assertEquals(EventStatus.registration_open, event.getStatus());

        verify(eventRepository).save(event);
    }

    @Test
    void changeStatus_RegistrationOpenToClosed_ShouldSuccess() {

        event.setStatus(EventStatus.registration_open);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        eventService.changeStatus(eventId, EventStatus.registration_closed);

        assertEquals(EventStatus.registration_closed, event.getStatus());

        verify(eventRepository).save(event);
    }

    @Test
    void changeStatus_RegistrationClosedToOngoing_ShouldSuccess() {

        event.setStatus(EventStatus.registration_closed);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        eventService.changeStatus(eventId, EventStatus.ongoing);

        assertEquals(EventStatus.ongoing, event.getStatus());

        verify(eventRepository).save(event);
    }

    @Test
    void changeStatus_OngoingToCompleted_ShouldSuccess() {

        event.setStatus(EventStatus.ongoing);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        eventService.changeStatus(eventId, EventStatus.completed);

        assertEquals(EventStatus.completed, event.getStatus());

        verify(eventRepository).save(event);
    }

    @Test
    void changeStatus_DraftToRegistrationOpen_ShouldThrowConflict() {

        event.setStatus(EventStatus.draft);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(ConflictException.class,
                () -> eventService.changeStatus(eventId, EventStatus.registration_open));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void changeStatus_DraftToCompleted_ShouldThrowConflict() {

        event.setStatus(EventStatus.draft);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(ConflictException.class,
                () -> eventService.changeStatus(eventId, EventStatus.completed));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void changeStatus_PublishedToOngoing_ShouldThrowConflict() {

        event.setStatus(EventStatus.published);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(ConflictException.class,
                () -> eventService.changeStatus(eventId, EventStatus.ongoing));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void changeStatus_RegistrationClosedToPublished_ShouldThrowConflict() {

        event.setStatus(EventStatus.registration_closed);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(ConflictException.class,
                () -> eventService.changeStatus(eventId, EventStatus.published));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void changeStatus_CompletedToDraft_ShouldThrowConflict() {

        event.setStatus(EventStatus.completed);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(ConflictException.class,
                () -> eventService.changeStatus(eventId, EventStatus.draft));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void changeStatus_CompletedToPublished_ShouldThrowConflict() {

        event.setStatus(EventStatus.completed);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        assertThrows(ConflictException.class,
                () -> eventService.changeStatus(eventId, EventStatus.published));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void create_shouldSuccess() {

        when(eventMapper.toEntity(request)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.create(request);

        assertNotNull(result);
        assertEquals(eventId, result.getId());

        verify(eventMapper).toEntity(request);
        verify(eventRepository).save(event);
        verify(eventMapper).toResponse(event);
    }

    @Test
    void update_shouldSuccess() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        EventResponse result = eventService.update(eventId, request);

        assertNotNull(result);

        verify(eventRepository).findById(eventId);
        verify(eventMapper).updateEntity(request, event);
        verify(eventRepository).save(event);
    }

    @Test
    void update_shouldThrow_whenEventNotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> eventService.update(eventId, request));

        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void eventFilter_shouldReturnList() {

        List<Event> events = List.of(event);
        List<EventResponse> responses = List.of(response);

        when(eventRepository.eventFilter(
                "Summer",
                2026,
                "draft"))
                .thenReturn(events);

        when(eventMapper.toResponseList(events))
                .thenReturn(responses);

        List<EventResponse> result = eventService.eventFilter("Summer", 2026, EventStatus.draft);

        assertEquals(1, result.size());

        verify(eventRepository).eventFilter(
                "Summer",
                2026,
                "draft");
    }

    @Test
    void eventFilter_shouldConvertBlankKeywordToNull() {

        List<Event> events = List.of(event);

        when(eventRepository.eventFilter(
                null,
                2026,
                "draft"))
                .thenReturn(events);

        when(eventMapper.toResponseList(events))
                .thenReturn(List.of(response));

        eventService.eventFilter(" ", 2026, EventStatus.draft);

        verify(eventRepository).eventFilter(
                null,
                2026,
                "draft");
    }

    @Test
    void eventFilter_shouldReturnAllStatus_whenStatusIsNull() {

        List<Event> events = List.of(event);

        when(eventRepository.eventFilter(
                "Summer",
                2026,
                null))
                .thenReturn(events);

        when(eventMapper.toResponseList(events))
                .thenReturn(List.of(response));

        List<EventResponse> result = eventService.eventFilter("Summer", 2026, null);

        assertEquals(1, result.size());

        verify(eventRepository).eventFilter(
                "Summer",
                2026,
                null);
    }

    @Test
    void changeStatus_shouldThrow_whenEventNotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> eventService.changeStatus(eventId, EventStatus.published));

        verify(eventRepository, never()).save(any());
    }

    @Test
    void eventById_shouldSuccess() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        EventResponse result = eventService.eventById(eventId);

        assertNotNull(result);
        assertEquals(eventId, result.getId());

        verify(eventRepository).findById(eventId);
    }

    @Test
    void eventById_shouldThrow_whenEventNotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> eventService.eventById(eventId));
    }
}
