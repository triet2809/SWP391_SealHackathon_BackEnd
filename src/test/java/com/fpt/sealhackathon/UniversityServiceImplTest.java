package com.fpt.sealhackathon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fpt.sealhackathon.dto.university.UniversityRequest;
import com.fpt.sealhackathon.dto.university.UniversityResponse;
import com.fpt.sealhackathon.entity.University;
import com.fpt.sealhackathon.exception.ConflictException;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.UniversityMapper;
import com.fpt.sealhackathon.repository.CampusRepository;
import com.fpt.sealhackathon.repository.UniversityRepository;
import com.fpt.sealhackathon.service.impl.UniversityServiceImpl;

@ExtendWith(MockitoExtension.class)
class UniversityServiceImplTest {

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private CampusRepository campusRepository;

    @Mock
    private UniversityMapper universityMapper;

    @InjectMocks
    private UniversityServiceImpl universityService;

    private UUID universityId;
    private University university;
    private UniversityRequest request;
    private UniversityResponse response;

    @BeforeEach
    void setUp() {

        universityId = UUID.randomUUID();

        university = University.builder()
                .id(universityId)
                .name("FPT University")
                .shortName("FPT")
                .country("Vietnam")
                .build();

        request = UniversityRequest.builder()
                .name("FPT University")
                .shortName("FPT")
                .country("Vietnam")
                .build();

        response = UniversityResponse.builder()
                .id(universityId)
                .name("FPT University")
                .shortName("FPT")
                .country("Vietnam")
                .build();
    }

    @Test
    void universityFilter_shouldReturnList() {

        List<University> universities = List.of(university);
        List<UniversityResponse> responses = List.of(response);

        when(universityRepository.universityFilter("FPT", null))
                .thenReturn(universities);

        when(universityMapper.toResponseList(universities))
                .thenReturn(responses);

        List<UniversityResponse> result = universityService.universityFilter("FPT", null);

        assertEquals(1, result.size());
        assertEquals("FPT University", result.get(0).getName());

        verify(universityRepository).universityFilter("FPT", null);
    }

    @Test
    void create_shouldSuccess() {

        when(universityMapper.toEntity(request))
                .thenReturn(university);

        when(universityRepository.save(university))
                .thenReturn(university);

        when(universityMapper.toResponse(university))
                .thenReturn(response);

        UniversityResponse result = universityService.create(request);

        assertNotNull(result);
        assertEquals("FPT University", result.getName());

        verify(universityRepository).save(university);
    }

    @Test
    void update_shouldSuccess() {

        when(universityRepository.findById(universityId))
                .thenReturn(Optional.of(university));

        when(universityRepository.save(university))
                .thenReturn(university);

        when(universityMapper.toResponse(university))
                .thenReturn(response);

        UniversityResponse result = universityService.update(universityId, request);

        assertNotNull(result);

        verify(universityMapper).updateEntityFromRequest(request, university);
        verify(universityRepository).save(university);
    }

    @Test
    void update_shouldThrow_whenUniversityNotFound() {

        when(universityRepository.findById(universityId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> universityService.update(universityId, request));

        verify(universityRepository).findById(universityId);
        verify(universityRepository, never()).save(any());
    }

    @Test
    void delete_shouldSuccess() {

        when(universityRepository.findById(universityId))
                .thenReturn(Optional.of(university));

        when(campusRepository.existsByUniversity_Id(universityId))
                .thenReturn(false);

        universityService.delete(universityId);

        verify(universityRepository).findById(universityId);
        verify(campusRepository).existsByUniversity_Id(universityId);
        verify(universityRepository).delete(university);
    }

    @Test
    void delete_shouldThrow_whenUniversityNotFound() {

        when(universityRepository.findById(universityId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> universityService.delete(universityId));

        verify(universityRepository).findById(universityId);
        verify(campusRepository, never()).existsByUniversity_Id(any());
        verify(universityRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrow_whenUniversityHasCampuses() {

        when(universityRepository.findById(universityId))
                .thenReturn(Optional.of(university));

        when(campusRepository.existsByUniversity_Id(universityId))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> universityService.delete(universityId));

        verify(universityRepository).findById(universityId);
        verify(campusRepository).existsByUniversity_Id(universityId);
        verify(universityRepository, never()).delete(any());
    }
}
