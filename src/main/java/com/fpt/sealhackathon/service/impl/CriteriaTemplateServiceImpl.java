package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.criteriatemplate.CriteriaTemplateRequest;
import com.fpt.sealhackathon.dto.criteriatemplate.CriteriaTemplateResponse;
import com.fpt.sealhackathon.entity.CriteriaTemplate;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.repository.CriteriaTemplateRepository;
import com.fpt.sealhackathon.service.CriteriaTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriteriaTemplateServiceImpl implements CriteriaTemplateService {

    private final CriteriaTemplateRepository criteriaTemplateRepository;

    @Override
    public CriteriaTemplateResponse createCriteriaTemplate(CriteriaTemplateRequest request) {
        CriteriaTemplate criteriaTemplate = CriteriaTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .defaultWeight(request.getDefaultWeight())
                .build();
        return mapToResponse(criteriaTemplateRepository.save(criteriaTemplate));
    }

    @Override
    public List<CriteriaTemplateResponse> getAllCriteriaTemplates() {
        return criteriaTemplateRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public CriteriaTemplateResponse getCriteriaTemplateById(UUID id) {
        return mapToResponse(findCriteriaTemplateById(id));
    }

    @Override
    public CriteriaTemplateResponse updateCriteriaTemplate(UUID id, CriteriaTemplateRequest request) {
        CriteriaTemplate criteriaTemplate = findCriteriaTemplateById(id);
        criteriaTemplate.setName(request.getName());
        criteriaTemplate.setDescription(request.getDescription());
        criteriaTemplate.setDefaultWeight(request.getDefaultWeight());
        return mapToResponse(criteriaTemplateRepository.save(criteriaTemplate));
    }

    @Override
    public void deleteCriteriaTemplate(UUID id) {
        criteriaTemplateRepository.delete(findCriteriaTemplateById(id));
    }

    private CriteriaTemplate findCriteriaTemplateById(UUID id) {
        return criteriaTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criteria template not found with id: " + id));
    }

    private CriteriaTemplateResponse mapToResponse(CriteriaTemplate criteriaTemplate) {
        return CriteriaTemplateResponse.builder()
                .id(criteriaTemplate.getId())
                .name(criteriaTemplate.getName())
                .description(criteriaTemplate.getDescription())
                .defaultWeight(criteriaTemplate.getDefaultWeight())
                .createdAt(criteriaTemplate.getCreatedAt())
                .updatedAt(criteriaTemplate.getUpdatedAt())
                .build();
    }
}
