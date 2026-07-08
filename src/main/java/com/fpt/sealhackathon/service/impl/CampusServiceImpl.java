package com.fpt.sealhackathon.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.campus.CampusRequest;
import com.fpt.sealhackathon.dto.campus.CampusResponse;
import com.fpt.sealhackathon.entity.Campus;
import com.fpt.sealhackathon.entity.University;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.CampusMapper;
import com.fpt.sealhackathon.repository.CampusRepository;
import com.fpt.sealhackathon.repository.UniversityRepository;
import com.fpt.sealhackathon.service.CampusService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {

    private final CampusRepository campusRepository;
    private final UniversityRepository universityRepository;
    private final CampusMapper campusMapper;

    @Override
    public CampusResponse create(UUID id, CampusRequest request) {
        Campus campus = campusMapper.toEntity(request);

        University university = universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));

        campus.setUniversity(university);

        return campusMapper.toResponse(campusRepository.save(campus));
    }

    @Override
    public CampusResponse update(UUID id, CampusRequest request) {

        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        campusMapper.updateEntityFromRequest(request, campus);

        if (request.getUniversityId() != null) {

            University university = universityRepository.findById(request.getUniversityId())
                    .orElseThrow(() -> new ResourceNotFoundException("University not found"));

            campus.setUniversity(university);
        }

        return campusMapper.toResponse(campusRepository.save(campus));
    }

    @Override
    public void delete(UUID id) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        campusRepository.delete(campus);
    }

    @Override
    public List<CampusResponse> campusFilter(UUID universityId, String keyword) {

        List<Campus> campuses = campusRepository.campusFilter(universityId, keyword);

        return campusMapper.toResponseList(campuses);
    }

    @Override
    public List<CampusResponse> campusByUniversity(UUID universityId) {
        List<Campus> campuses = campusRepository.findByUniversity_Id(universityId);

        return campusMapper.toResponseList(campuses);
    }

}
