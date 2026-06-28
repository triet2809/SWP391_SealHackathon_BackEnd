package com.fpt.sealhackathon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.criteriaTemplate.CriteriaTemplateRequest;
import com.fpt.sealhackathon.dto.criteriaTemplate.CriteriaTemplateResponse;

@Service
public interface CriteriaTemplateService {

    CriteriaTemplateResponse create(CriteriaTemplateRequest request);

    CriteriaTemplateResponse update(UUID id, CriteriaTemplateRequest request);

    void delete(UUID id);

    List<CriteriaTemplateResponse> filter(String keyword);
}