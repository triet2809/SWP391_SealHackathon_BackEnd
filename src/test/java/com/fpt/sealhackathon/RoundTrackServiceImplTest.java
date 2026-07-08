package com.fpt.sealhackathon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
import com.fpt.sealhackathon.service.impl.EventServiceImpl;
import com.fpt.sealhackathon.service.impl.RoundTrackServiceImpl;

@ExtendWith(MockitoExtension.class)
class RoundTrackServiceImplTest {

    @Mock
    private RoundTrackRepository roundTrackRepository;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RoundTrackMapper roundTrackMapper;

    private RoundTrackService roundTrackService;

    private UUID eventId;
    private UUID roundId;
    private UUID roundTrackId;

    private Event event;
    private Round round;
    private RoundTrack roundTrack;

    private RoundTrackRequest request;
    private RoundTrackResponse response;

    @BeforeEach
    void setUp() {

        roundTrackService = new RoundTrackServiceImpl(
                roundTrackRepository,
                roundRepository,
                eventRepository,
                roundTrackMapper);

        eventId = UUID.randomUUID();
        roundId = UUID.randomUUID();
        roundTrackId = UUID.randomUUID();

        event = Event.builder()
                .id(eventId)
                .title("Hackathon")
                .build();

        round = Round.builder()
                .id(roundId)
                .event(event)
                .name("Round 1")
                .build();

        request = RoundTrackRequest.builder()
                .eventId(eventId)
                .roundId(roundId)
                .name("Backend Track")
                .challengeTitle("REST API")
                .challengeDescription("Implement REST API")
                .challengeFileUrl("https://abc.com/file.pdf")
                .maxTeams(20)
                .topNToPromote(10)
                .displayOrder(1)
                .isFinalSharedTrack(false)
                .build();

        roundTrack = RoundTrack.builder()
                .id(roundTrackId)
                .event(event)
                .round(round)
                .name("Backend Track")
                .challengeTitle("REST API")
                .challengeDescription("Implement REST API")
                .challengeFileUrl("https://abc.com/file.pdf")
                .maxTeams(20)
                .topNToPromote(10)
                .displayOrder(1)
                .isFinalSharedTrack(false)
                .build();

        response = RoundTrackResponse.builder()
                .id(roundTrackId)
                .name("Backend Track")
                .build();
    }

    @Test
    void create_shouldSuccess() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        when(roundTrackMapper.toEntity(request))
                .thenReturn(roundTrack);

        when(roundTrackRepository.save(roundTrack))
                .thenReturn(roundTrack);

        when(roundTrackMapper.toResponse(roundTrack))
                .thenReturn(response);

        RoundTrackResponse result = roundTrackService.create(roundId, request);

        assertNotNull(result);

        verify(eventRepository).findById(eventId);
        verify(roundRepository).findById(roundId);
        verify(roundTrackRepository).save(roundTrack);
    }

    @Test
    void create_shouldThrow_whenEventNotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundTrackService.create(roundId, request));

        verify(roundRepository, never())
                .findById(any());

        verify(roundTrackRepository, never())
                .save(any());
    }

    @Test
    void create_shouldThrow_whenRoundNotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundTrackService.create(roundId, request));

        verify(roundTrackRepository, never())
                .save(any());
    }

    @Test
    void create_shouldThrow_whenRoundNotBelongToEvent() {

        Event anotherEvent = Event.builder()
                .id(UUID.randomUUID())
                .build();

        Round anotherRound = Round.builder()
                .event(anotherEvent)
                .build();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(anotherRound));

        assertThrows(ConflictException.class,
                () -> roundTrackService.create(roundId, request));

        verify(roundTrackRepository, never())
                .save(any());
    }

    @Test
    void create_shouldThrow_whenTopNGreaterThanMaxTeams() {

        request.setMaxTeams(10);
        request.setTopNToPromote(11);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        assertThrows(ConflictException.class,
                () -> roundTrackService.create(roundId, request));

        verify(roundTrackRepository, never())
                .save(any());
    }

    @Test
    void create_shouldAllowTopNEqualsMaxTeams() {

        request.setMaxTeams(10);
        request.setTopNToPromote(10);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        when(roundTrackMapper.toEntity(request))
                .thenReturn(roundTrack);

        when(roundTrackRepository.save(roundTrack))
                .thenReturn(roundTrack);

        when(roundTrackMapper.toResponse(roundTrack))
                .thenReturn(response);

        RoundTrackResponse result = roundTrackService.create(roundId, request);

        assertNotNull(result);
    }

    @Test
    void update_shouldSuccess() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        doNothing().when(roundTrackMapper)
                .updateEntity(request, roundTrack);

        when(roundTrackRepository.save(roundTrack))
                .thenReturn(roundTrack);

        when(roundTrackMapper.toResponse(roundTrack))
                .thenReturn(response);

        RoundTrackResponse result = roundTrackService.update(roundTrackId, request);

        assertNotNull(result);

        verify(roundTrackRepository).findById(roundTrackId);
        verify(eventRepository).findById(eventId);
        verify(roundRepository).findById(roundId);
        verify(roundTrackMapper).updateEntity(request, roundTrack);
        verify(roundTrackRepository).save(roundTrack);
    }

    @Test
    void update_shouldThrow_whenRoundTrackNotFound() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundTrackService.update(roundTrackId, request));

        verify(eventRepository, never()).findById(any());
        verify(roundRepository, never()).findById(any());
        verify(roundTrackRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenEventNotFound() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundTrackService.update(roundTrackId, request));

        verify(roundRepository, never()).findById(any());
        verify(roundTrackRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenRoundNotFound() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundTrackService.update(roundTrackId, request));

        verify(roundTrackRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenRoundNotBelongToEvent() {

        Event anotherEvent = Event.builder()
                .id(UUID.randomUUID())
                .build();

        Round anotherRound = Round.builder()
                .id(roundId)
                .event(anotherEvent)
                .build();

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(anotherRound));

        assertThrows(ConflictException.class,
                () -> roundTrackService.update(roundTrackId, request));

        verify(roundTrackRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenTopNGreaterThanMaxTeams() {

        request.setMaxTeams(10);
        request.setTopNToPromote(20);

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        assertThrows(ConflictException.class,
                () -> roundTrackService.update(roundTrackId, request));

        verify(roundTrackRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowTopNEqualsMaxTeams() {

        request.setMaxTeams(10);
        request.setTopNToPromote(10);

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        doNothing().when(roundTrackMapper)
                .updateEntity(request, roundTrack);

        when(roundTrackRepository.save(roundTrack))
                .thenReturn(roundTrack);

        when(roundTrackMapper.toResponse(roundTrack))
                .thenReturn(response);

        RoundTrackResponse result = roundTrackService.update(roundTrackId, request);

        assertNotNull(result);

        verify(roundTrackRepository).save(roundTrack);
    }

    @Test
    void update_shouldAllowTopNEqualsZero() {

        request.setMaxTeams(10);
        request.setTopNToPromote(0);

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        doNothing().when(roundTrackMapper)
                .updateEntity(request, roundTrack);

        when(roundTrackRepository.save(roundTrack))
                .thenReturn(roundTrack);

        when(roundTrackMapper.toResponse(roundTrack))
                .thenReturn(response);

        RoundTrackResponse result = roundTrackService.update(roundTrackId, request);

        assertNotNull(result);
    }

    @Test
    void delete_shouldSuccess() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        doNothing().when(roundTrackRepository).delete(roundTrack);
        doNothing().when(roundTrackRepository).flush();

        roundTrackService.delete(roundTrackId);

        verify(roundTrackRepository).findById(roundTrackId);
        verify(roundTrackRepository).delete(roundTrack);
        verify(roundTrackRepository).flush();
    }

    @Test
    void delete_shouldThrow_whenRoundTrackNotFound() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundTrackService.delete(roundTrackId));

        verify(roundTrackRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrow_whenRoundTrackInUse() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        doNothing().when(roundTrackRepository).delete(roundTrack);

        doThrow(DataIntegrityViolationException.class)
                .when(roundTrackRepository)
                .flush();

        assertThrows(ConflictException.class,
                () -> roundTrackService.delete(roundTrackId));

        verify(roundTrackRepository).delete(roundTrack);
        verify(roundTrackRepository).flush();
    }

    @Test
    void roundTrackFilter_shouldReturnList() {

        List<RoundTrack> entities = List.of(roundTrack);
        List<RoundTrackResponse> responses = List.of(response);

        when(roundTrackRepository.roundTrackFilter(
                eventId,
                roundId,
                "Backend"))
                .thenReturn(entities);

        when(roundTrackMapper.toResponseList(entities))
                .thenReturn(responses);

        List<RoundTrackResponse> result = roundTrackService.roundTrackFilter(
                eventId,
                roundId,
                "Backend");

        assertEquals(1, result.size());

        verify(roundTrackRepository)
                .roundTrackFilter(eventId, roundId, "Backend");
    }

    @Test
    void roundTrackByTrack_shouldReturnList() {

        List<RoundTrack> entities = List.of(roundTrack);

        when(roundTrackRepository.findByRound_Id(roundId))
                .thenReturn(entities);

        when(roundTrackMapper.toResponseList(entities))
                .thenReturn(List.of(response));

        List<RoundTrackResponse> result = roundTrackService.roundTrackByTrack(roundId);

        assertEquals(1, result.size());

        verify(roundTrackRepository)
                .findByRound_Id(roundId);
    }

    @Test
    void roundTrackById_shouldSuccess() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(roundTrackMapper.toResponse(roundTrack))
                .thenReturn(response);

        RoundTrackResponse result = roundTrackService.roundTrackById(roundTrackId);

        assertNotNull(result);

        verify(roundTrackRepository).findById(roundTrackId);
    }

    @Test
    void roundTrackById_shouldThrow_whenNotFound() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundTrackService.roundTrackById(roundTrackId));

        verify(roundTrackMapper, never()).toResponse(any());
    }

    @Test
    void updatePromotionRule_shouldSuccess() {

        roundTrack.setMaxTeams(20);

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(roundTrackRepository.save(roundTrack))
                .thenReturn(roundTrack);

        when(roundTrackMapper.toResponse(roundTrack))
                .thenReturn(response);

        RoundTrackResponse result = roundTrackService.updatePromotionRule(roundTrackId, 10);

        assertNotNull(result);
        assertEquals(10, roundTrack.getTopNToPromote());

        verify(roundTrackRepository).save(roundTrack);
    }

    @Test
    void updatePromotionRule_shouldThrow_whenRoundTrackNotFound() {

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundTrackService.updatePromotionRule(roundTrackId, 5));

        verify(roundTrackRepository, never()).save(any());
    }

    @Test
    void updatePromotionRule_shouldThrow_whenTopNGreaterThanMaxTeams() {

        roundTrack.setMaxTeams(10);

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        assertThrows(ConflictException.class,
                () -> roundTrackService.updatePromotionRule(roundTrackId, 20));

        verify(roundTrackRepository, never()).save(any());
    }

    @Test
    void updatePromotionRule_shouldAllowTopNEqualsMaxTeams() {

        roundTrack.setMaxTeams(10);

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(roundTrackRepository.save(roundTrack))
                .thenReturn(roundTrack);

        when(roundTrackMapper.toResponse(roundTrack))
                .thenReturn(response);

        RoundTrackResponse result = roundTrackService.updatePromotionRule(roundTrackId, 10);

        assertNotNull(result);
        assertEquals(10, roundTrack.getTopNToPromote());
    }

    @Test
    void updatePromotionRule_shouldAllowTopNEqualsZero() {

        roundTrack.setMaxTeams(10);

        when(roundTrackRepository.findById(roundTrackId))
                .thenReturn(Optional.of(roundTrack));

        when(roundTrackRepository.save(roundTrack))
                .thenReturn(roundTrack);

        when(roundTrackMapper.toResponse(roundTrack))
                .thenReturn(response);

        RoundTrackResponse result = roundTrackService.updatePromotionRule(roundTrackId, 0);

        assertNotNull(result);
        assertEquals(0, roundTrack.getTopNToPromote());
    }

}
