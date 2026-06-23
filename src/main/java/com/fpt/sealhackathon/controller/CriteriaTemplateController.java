package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.criteriatemplate.CriteriaTemplateRequest;
import com.fpt.sealhackathon.dto.criteriatemplate.CriteriaTemplateResponse;
import com.fpt.sealhackathon.service.CriteriaTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/criteria-templates")
@RequiredArgsConstructor
public class CriteriaTemplateController {

    private final CriteriaTemplateService criteriaTemplateService;

    @PostMapping
    public ResponseEntity<CriteriaTemplateResponse> createCriteriaTemplate(
            @Valid @RequestBody CriteriaTemplateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(criteriaTemplateService.createCriteriaTemplate(request));
    }

    @GetMapping
    public ResponseEntity<List<CriteriaTemplateResponse>> getAllCriteriaTemplates() {
        return ResponseEntity.ok(criteriaTemplateService.getAllCriteriaTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CriteriaTemplateResponse> getCriteriaTemplateById(@PathVariable UUID id) {
        return ResponseEntity.ok(criteriaTemplateService.getCriteriaTemplateById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CriteriaTemplateResponse> updateCriteriaTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody CriteriaTemplateRequest request
    ) {
        return ResponseEntity.ok(criteriaTemplateService.updateCriteriaTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCriteriaTemplate(@PathVariable UUID id) {
        criteriaTemplateService.deleteCriteriaTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
