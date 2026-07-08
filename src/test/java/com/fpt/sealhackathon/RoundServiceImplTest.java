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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.fpt.sealhackathon.dto.round.RoundRequest;
import com.fpt.sealhackathon.dto.round.RoundResponse;
import com.fpt.sealhackathon.dto.round.RoundUpsertRequest;
import com.fpt.sealhackathon.entity.Event;
import com.fpt.sealhackathon.entity.Round;
import com.fpt.sealhackathon.entity.enums.RoundStatus;
import com.fpt.sealhackathon.exception.ConflictException;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.RoundMapper;
import com.fpt.sealhackathon.repository.EventRepository;
import com.fpt.sealhackathon.repository.RoundRepository;
import com.fpt.sealhackathon.service.impl.RoundServiceImpl;

@ExtendWith(MockitoExtension.class)
class RoundServiceImplTest {

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RoundMapper roundMapper;

    @InjectMocks
    private RoundServiceImpl roundService;

    private UUID eventId;
    private UUID roundId;

    private Event event;
    private Round round;

    private RoundRequest request;
    private RoundUpsertRequest upsertRequest;

    private RoundResponse response;

    @BeforeEach
    void setUp() {

        eventId = UUID.randomUUID();
        roundId = UUID.randomUUID();

        event = Event.builder()
                .id(eventId)
                .title("Hackathon 2026")
                .build();

        request = RoundRequest.builder()
                .name("Round 1")
                .description("Idea")
                .sequenceNumber(1)
                .build();

        upsertRequest = RoundUpsertRequest.builder()
                .eventId(eventId)
                .name("Round 1 Updated")
                .description("Updated")
                .sequenceNumber(1)
                .build();

        round = Round.builder()
                .id(roundId)
                .event(event)
                .name("Round 1")
                .description("Idea")
                .sequenceNumber(1)
                .status(RoundStatus.draft)
                .build();

        response = RoundResponse.builder()
                .id(roundId)
                .name("Round 1")
                .description("Idea")
                .sequenceNumber(1)
                .status(RoundStatus.draft)
                .build();
    }

    @Test
    void create_shouldSuccess() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.existsByEvent_IdAndSequenceNumber(eventId, 1))
                .thenReturn(false);

        when(roundMapper.toEntity(request))
                .thenReturn(round);

        when(roundRepository.save(round))
                .thenReturn(round);

        when(roundMapper.toResponse(round))
                .thenReturn(response);

        RoundResponse result = roundService.create(eventId, request);

        assertNotNull(result);
        assertEquals(response.getId(), result.getId());
        assertEquals(RoundStatus.draft, round.getStatus());

        verify(eventRepository).findById(eventId);
        verify(roundRepository).existsByEvent_IdAndSequenceNumber(eventId, 1);
        verify(roundMapper).toEntity(request);
        verify(roundRepository).save(round);
        verify(roundMapper).toResponse(round);
    }

    @Test
    void create_shouldThrow_whenEventNotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundService.create(eventId, request));

        verify(eventRepository).findById(eventId);

        verify(roundRepository, never())
                .existsByEvent_IdAndSequenceNumber(any(), any());

        verify(roundRepository, never())
                .save(any());

        verify(roundMapper, never())
                .toEntity(any());
    }

    @Test
    void create_shouldThrow_whenSequenceNumberAlreadyExists() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.existsByEvent_IdAndSequenceNumber(eventId, 1))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> roundService.create(eventId, request));

        verify(eventRepository).findById(eventId);

        verify(roundRepository)
                .existsByEvent_IdAndSequenceNumber(eventId, 1);

        verify(roundRepository, never())
                .save(any());

        verify(roundMapper, never())
                .toEntity(any());
    }

    @Test
    void create_shouldSetDraftStatus() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.existsByEvent_IdAndSequenceNumber(eventId, 1))
                .thenReturn(false);

        Round entity = Round.builder()
                .sequenceNumber(1)
                .build();

        when(roundMapper.toEntity(request))
                .thenReturn(entity);

        when(roundRepository.save(entity))
                .thenReturn(entity);

        when(roundMapper.toResponse(entity))
                .thenReturn(response);

        roundService.create(eventId, request);

        assertEquals(RoundStatus.draft, entity.getStatus());

        verify(roundRepository).save(entity);
    }

    @Test
    void update_shouldSuccess() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.existsByEvent_IdAndSequenceNumberAndIdNot(
                eventId,
                upsertRequest.getSequenceNumber(),
                roundId))
                .thenReturn(false);

        doNothing().when(roundMapper)
                .updateEntityFromRequest(upsertRequest, round);

        when(roundRepository.save(round))
                .thenReturn(round);

        when(roundMapper.toResponse(round))
                .thenReturn(response);

        RoundResponse result = roundService.update(roundId, upsertRequest);

        assertNotNull(result);

        verify(roundRepository).findById(roundId);
        verify(eventRepository).findById(eventId);
        verify(roundMapper).updateEntityFromRequest(upsertRequest, round);
        verify(roundRepository).save(round);
    }

    @Test
    void update_shouldThrow_whenRoundNotFound() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundService.update(roundId, upsertRequest));

        verify(roundRepository).findById(roundId);

        verify(eventRepository, never()).findById(any());

        verify(roundRepository, never())
                .existsByEvent_IdAndSequenceNumberAndIdNot(any(), any(), any());

        verify(roundRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenEventNotFound() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundService.update(roundId, upsertRequest));

        verify(roundRepository).findById(roundId);
        verify(eventRepository).findById(eventId);

        verify(roundRepository, never())
                .existsByEvent_IdAndSequenceNumberAndIdNot(any(), any(), any());

        verify(roundRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenChangeEvent() {

        UUID anotherEvent = UUID.randomUUID();

        upsertRequest.setEventId(anotherEvent);

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        when(eventRepository.findById(anotherEvent))
                .thenReturn(Optional.of(
                        Event.builder()
                                .id(anotherEvent)
                                .build()));

        assertThrows(ConflictException.class,
                () -> roundService.update(roundId, upsertRequest));

        verify(roundRepository, never())
                .existsByEvent_IdAndSequenceNumberAndIdNot(any(), any(), any());

        verify(roundRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenSequenceNumberAlreadyExists() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(roundRepository.existsByEvent_IdAndSequenceNumberAndIdNot(
                eventId,
                upsertRequest.getSequenceNumber(),
                roundId))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> roundService.update(roundId, upsertRequest));

        verify(roundRepository)
                .existsByEvent_IdAndSequenceNumberAndIdNot(
                        eventId,
                        upsertRequest.getSequenceNumber(),
                        roundId);

        verify(roundRepository, never()).save(any());
    }

    @Test
    void roundFilter_shouldReturnList() {

        List<Round> rounds = List.of(round);
        List<RoundResponse> responses = List.of(response);

        when(roundRepository.roundFilter(eventId, "Round"))
                .thenReturn(rounds);

        when(roundMapper.toResponseList(rounds))
                .thenReturn(responses);

        List<RoundResponse> result = roundService.roundFilter(eventId, "Round");

        assertEquals(1, result.size());

        verify(roundRepository)
                .roundFilter(eventId, "Round");

        verify(roundMapper)
                .toResponseList(rounds);
    }

    @Test
    void changeStatus_shouldSuccess() {

        round.setStatus(RoundStatus.draft);

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        when(roundRepository.save(round))
                .thenReturn(round);

        when(roundMapper.toResponse(round))
                .thenReturn(response);

        RoundResponse result = roundService.changeStatus(roundId, RoundStatus.open);

        assertNotNull(result);
        assertEquals(RoundStatus.open, round.getStatus());

        verify(roundRepository).save(round);
    }

    @Test
    void changeStatus_shouldThrow_whenRoundNotFound() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundService.changeStatus(roundId, RoundStatus.open));

        verify(roundRepository, never())
                .save(any());
    }

    @Test
    void changeStatus_shouldThrow_whenInvalidTransition() {

        round.setStatus(RoundStatus.draft);

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        assertThrows(ConflictException.class,
                () -> roundService.changeStatus(
                        roundId,
                        RoundStatus.completed));

        verify(roundRepository, never())
                .save(any());
    }

    @ParameterizedTest
    @CsvSource({
            "draft,open",
            "open,submission_closed",
            "submission_closed,scoring",
            "scoring,ranking_published",
            "ranking_published,completed"
    })
    void changeStatus_shouldSuccess(
            RoundStatus current,
            RoundStatus next) {

        round.setStatus(current);

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        when(roundRepository.save(round))
                .thenReturn(round);

        when(roundMapper.toResponse(round))
                .thenReturn(response);

        roundService.changeStatus(roundId, next);

        assertEquals(next, round.getStatus());

        verify(roundRepository).save(round);
    }

    @Test
    void roundByEventId_shouldReturnList() {

        List<Round> rounds = List.of(round);

        when(roundRepository.findByEvent_Id(eventId))
                .thenReturn(rounds);

        when(roundMapper.toResponseList(rounds))
                .thenReturn(List.of(response));

        List<RoundResponse> result = roundService.roundByEventId(eventId);

        assertEquals(1, result.size());

        verify(roundRepository)
                .findByEvent_Id(eventId);
    }

    @Test
    void roundById_shouldReturnResponse() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        when(roundMapper.toResponse(round))
                .thenReturn(response);

        RoundResponse result = roundService.rounndById(roundId);

        assertNotNull(result);

        verify(roundRepository)
                .findById(roundId);
    }

    @Test
    void roundById_shouldThrow_whenNotFound() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundService.rounndById(roundId));

        verify(roundMapper, never())
                .toResponse(any());
    }

    @Test
    void delete_shouldSuccess() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        doNothing().when(roundRepository)
                .delete(round);

        doNothing().when(roundRepository)
                .flush();

        roundService.delete(roundId);

        verify(roundRepository).delete(round);
        verify(roundRepository).flush();
    }

    @Test
    void delete_shouldThrow_whenNotFound() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roundService.delete(roundId));

        verify(roundRepository, never())
                .delete(any());
    }

    @Test
    void delete_shouldThrow_whenRoundInUse() {

        when(roundRepository.findById(roundId))
                .thenReturn(Optional.of(round));

        doNothing().when(roundRepository)
                .delete(round);

        doThrow(DataIntegrityViolationException.class)
                .when(roundRepository)
                .flush();

        assertThrows(ConflictException.class,
                () -> roundService.delete(roundId));

        verify(roundRepository).delete(round);
        verify(roundRepository).flush();
    }
}
