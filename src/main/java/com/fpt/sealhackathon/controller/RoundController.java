package com.fpt.sealhackathon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.dto.round.RoundRequest;
import com.fpt.sealhackathon.dto.round.RoundResponse;
import com.fpt.sealhackathon.entity.enums.RoundStatus;
import com.fpt.sealhackathon.service.RoundService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final RoundService roundService;

    @GetMapping
    @Operation(summary = "Get round list", description = "Filter by round and keyword")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<List<RoundResponse>>> roundFilter(
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(ApiResponse.success(
                "Get rounds successfully",
                roundService.roundFilter(eventId, keyword)));
    }

    @PostMapping
    @Operation(summary = "Create round", description = "Create new round")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<RoundResponse>> create(
            @RequestBody @Valid RoundRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Create round successfully",
                roundService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update round", description = "Update round")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<RoundResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid RoundRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Update round successfully",
                roundService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change round status", description = "Update round status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<RoundResponse>> changeStatus(
            @PathVariable UUID id,
            @RequestParam RoundStatus status) {

        return ResponseEntity.ok(ApiResponse.success(
                "Change round status successfully",
                roundService.changeStatus(id, status)));
    }
}
