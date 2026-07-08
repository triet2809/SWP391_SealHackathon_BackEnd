package com.fpt.sealhackathon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

import com.fpt.sealhackathon.dto.campus.CampusRequest;
import com.fpt.sealhackathon.dto.campus.CampusResponse;
import com.fpt.sealhackathon.entity.Campus;
import com.fpt.sealhackathon.entity.University;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.CampusMapper;
import com.fpt.sealhackathon.repository.CampusRepository;
import com.fpt.sealhackathon.repository.UniversityRepository;
import com.fpt.sealhackathon.repository.UserRepository;
import com.fpt.sealhackathon.service.impl.CampusServiceImpl;

@ExtendWith(MockitoExtension.class)
class CampusServiceImplTest {

        @Mock
        private CampusRepository campusRepository;

        @Mock
        private UniversityRepository universityRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private CampusMapper campusMapper;

        @InjectMocks
        private CampusServiceImpl campusService;

        private UUID campusId;
        private UUID universityId;

        private University university;
        private Campus campus;
        private CampusRequest request;
        private CampusResponse response;

        @BeforeEach
        void setUp() {

                campusId = UUID.randomUUID();
                universityId = UUID.randomUUID();

                university = University.builder()
                                .id(universityId)
                                .name("FPT University")
                                .build();

                campus = Campus.builder()
                                .id(campusId)
                                .name("HCM Campus")
                                .city("HCM")
                                .address("District 9")
                                .university(university)
                                .build();

                request = CampusRequest.builder()
                                .name("HCM Campus")
                                .city("HCM")
                                .address("District 9")
                                .build();

                response = CampusResponse.builder()
                                .id(campusId)
                                .name("HCM Campus")
                                .city("HCM")
                                .address("District 9")
                                .build();
        }

        @Test
        void create_shouldSuccess() {

                when(universityRepository.findById(universityId))
                                .thenReturn(Optional.of(university));

                when(campusMapper.toEntity(request))
                                .thenReturn(campus);

                when(campusRepository.save(any(Campus.class)))
                                .thenReturn(campus);

                when(campusMapper.toResponse(campus))
                                .thenReturn(response);

                CampusResponse result = campusService.create(universityId, request);

                assertNotNull(result);
                assertEquals("HCM Campus", result.getName());

                verify(universityRepository).findById(universityId);
                verify(campusRepository).save(any(Campus.class));
        }

        @Test
        void create_shouldThrow_whenUniversityNotFound() {

                when(universityRepository.findById(universityId))
                                .thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class,
                                () -> campusService.create(universityId, request));

                verify(campusRepository, never()).save(any());
        }

        @Test
        void update_shouldSuccess() {

                when(campusRepository.findById(campusId))
                                .thenReturn(Optional.of(campus));

                when(campusRepository.save(campus))
                                .thenReturn(campus);

                when(campusMapper.toResponse(campus))
                                .thenReturn(response);

                CampusResponse result = campusService.update(campusId, request);

                assertNotNull(result);

                verify(campusMapper).updateEntityFromRequest(request, campus);
                verify(campusRepository).save(campus);
        }

        @Test
        void update_shouldThrow_whenCampusNotFound() {

                when(campusRepository.findById(campusId))
                                .thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class,
                                () -> campusService.update(campusId, request));
        }

        @Test
        void delete_shouldSuccess() {

                UUID campusId = UUID.randomUUID();

                Campus campus = Campus.builder()
                                .id(campusId)
                                .build();

                when(campusRepository.findById(campusId))
                                .thenReturn(Optional.of(campus));

                campusService.delete(campusId);

                verify(campusRepository).findById(campusId);
                verify(campusRepository).delete(campus);
                verifyNoMoreInteractions(campusRepository);
        }

        @Test
        void delete_shouldThrow_whenCampusNotFound() {

                UUID campusId = UUID.randomUUID();

                when(campusRepository.findById(campusId))
                                .thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class,
                                () -> campusService.delete(campusId));

                verify(campusRepository).findById(campusId);
                verify(campusRepository, never()).delete(any(Campus.class));
        }

        @Test
        void campusFilter_shouldReturnList() {

                List<Campus> campuses = List.of(campus);
                List<CampusResponse> responses = List.of(response);

                when(campusRepository.campusFilter(universityId, "FPT"))
                                .thenReturn(campuses);

                when(campusMapper.toResponseList(campuses))
                                .thenReturn(responses);

                List<CampusResponse> result = campusService.campusFilter(universityId, "FPT");

                assertEquals(1, result.size());

                verify(campusRepository).campusFilter(universityId, "FPT");
        }

        @Test
        void campusByUniversity_shouldReturnList() {

                List<Campus> campuses = List.of(campus);
                List<CampusResponse> responses = List.of(response);

                when(campusRepository.findByUniversity_Id(universityId))
                                .thenReturn(campuses);

                when(campusMapper.toResponseList(campuses))
                                .thenReturn(responses);

                List<CampusResponse> result = campusService.campusByUniversity(universityId);

                assertEquals(1, result.size());

                verify(campusRepository).findByUniversity_Id(universityId);
        }
}
