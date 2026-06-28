package com.fpt.sealhackathon.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.criteriaTemplate.CriteriaTemplateRequest;
import com.fpt.sealhackathon.dto.criteriaTemplate.CriteriaTemplateResponse;
import com.fpt.sealhackathon.entity.CriteriaTemplate;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.mapper.CriteriaTemplateMapper;
import com.fpt.sealhackathon.repository.CriteriaTemplateRepository;
import com.fpt.sealhackathon.service.CriteriaTemplateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CriteriaTemplateServiceImpl implements CriteriaTemplateService {

    private final CriteriaTemplateRepository repository;
    private final CriteriaTemplateMapper mapper;

    @Override
    public CriteriaTemplateResponse create(CriteriaTemplateRequest request) {

        CriteriaTemplate entity = mapper.toEntity(request);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public CriteriaTemplateResponse update(UUID id, CriteriaTemplateRequest request) {

        CriteriaTemplate entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criteria template not found"));

        mapper.updateEntity(request, entity);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(UUID id) {
        CriteriaTemplate entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criteria template not found"));

        repository.delete(entity);
    }

    @Override
    public List<CriteriaTemplateResponse> filter(String keyword) {
        return mapper.toResponseList(repository.filter(keyword));
    }
}
