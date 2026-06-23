package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.criteriatemplate.CriteriaTemplateRequest;
import com.fpt.sealhackathon.dto.criteriatemplate.CriteriaTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface CriteriaTemplateService {

    CriteriaTemplateResponse createCriteriaTemplate(CriteriaTemplateRequest request);

    List<CriteriaTemplateResponse> getAllCriteriaTemplates();

    CriteriaTemplateResponse getCriteriaTemplateById(UUID id);

    CriteriaTemplateResponse updateCriteriaTemplate(UUID id, CriteriaTemplateRequest request);

    void deleteCriteriaTemplate(UUID id);
}
