package com.fpt.sealhackathon.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.university.UniversityRequest;
import com.fpt.sealhackathon.dto.university.UniversityResponse;
import com.fpt.sealhackathon.entity.University;
import com.fpt.sealhackathon.exception.ConflictException;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.UniversityMapper;
import com.fpt.sealhackathon.repository.CampusRepository;
import com.fpt.sealhackathon.repository.UniversityRepository;
import com.fpt.sealhackathon.service.UniversityService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;
    private final CampusRepository campusRepository;
    private final UniversityMapper universityMapper;

    @Override
    public List<UniversityResponse> universityFilter(String keyword, List<UUID> campusIds) {

        List<University> universities = universityRepository.universityFilter(keyword, campusIds);

        return universityMapper.toResponseList(universities);
    }

    @Override
    public UniversityResponse create(UniversityRequest request) {

        University university = universityMapper.toEntity(request);

        return universityMapper.toResponse(universityRepository.save(university));
    }

    @Override
    public UniversityResponse update(UUID id, UniversityRequest request) {

        University university = universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));

        universityMapper.updateEntityFromRequest(request, university);

        return universityMapper.toResponse(universityRepository.save(university));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));

        if (campusRepository.existsByUniversity_Id(id)) {
            throw new ConflictException("Cannot delete university because it still has campuses.");
        }

        universityRepository.delete(university);
    }

}
