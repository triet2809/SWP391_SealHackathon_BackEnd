package com.fpt.sealhackathon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.university.UniversityRequest;
import com.fpt.sealhackathon.dto.university.UniversityResponse;

@Service
public interface UniversityService {

    List<UniversityResponse> universityFilter(
            String keyword,
            List<UUID> campusIds);

    UniversityResponse create(UniversityRequest request);

    UniversityResponse update(UUID id, UniversityRequest request);

    void delete(UUID id);

}
