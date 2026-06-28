package com.fpt.sealhackathon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.dto.criteriaTemplate.CriteriaTemplateRequest;
import com.fpt.sealhackathon.dto.criteriaTemplate.CriteriaTemplateResponse;
import com.fpt.sealhackathon.service.CriteriaTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/criteria-templates")
@RequiredArgsConstructor
public class CriteriaTemplateController {

    private final CriteriaTemplateService service;

    @GetMapping
    @Operation(summary = "Get criteria templates", description = "Filter Get criteria templates")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<List<CriteriaTemplateResponse>>> filter(
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get criteria templates successfully",
                        service.filter(keyword)));
    }

    @PostMapping
    @Operation(summary = "Create criteria template", description = "Create criteria template")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<CriteriaTemplateResponse>> create(
            @RequestBody @Valid CriteriaTemplateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create criteria template successfully",
                        service.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update criteria template", description = "Update criteria template")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<CriteriaTemplateResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid CriteriaTemplateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Update criteria template successfully",
                        service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete criteria template", description = "Delete criteria template")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {

        service.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success("Delete successfully", null));
    }
}
