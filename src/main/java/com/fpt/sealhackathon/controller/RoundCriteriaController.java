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

import com.fpt.sealhackathon.dto.RoundCriteria.RoundCriteriaRequest;
import com.fpt.sealhackathon.dto.RoundCriteria.RoundCriteriaResponse;
import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.service.RoundCriteriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/round-criteria")
@RequiredArgsConstructor
public class RoundCriteriaController {

    private final RoundCriteriaService service;

    @GetMapping
    @Operation(summary = "Get round criteria list", description = "Filter by round criteria and keyword")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<List<RoundCriteriaResponse>>> filter(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(required = false) UUID roundTrackId,
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get round criteria successfully",
                        service.filter(eventId, roundId, roundTrackId, keyword)));
    }

    @PostMapping
    @Operation(summary = "Create round criteria", description = "Create round criteria")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<RoundCriteriaResponse>> create(
            @RequestBody @Valid RoundCriteriaRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create round criteria successfully",
                        service.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update round criteria", description = "Update round criteria")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<RoundCriteriaResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid RoundCriteriaRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Update round criteria successfully",
                        service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete round criteria", description = "Delete round criteria")
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
