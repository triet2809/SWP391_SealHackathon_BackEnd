package com.fpt.sealhackathon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.dto.event.EventRequest;
import com.fpt.sealhackathon.dto.event.EventResponse;
import com.fpt.sealhackathon.dto.event.EventStatusRequest;
import com.fpt.sealhackathon.entity.enums.EventStatus;
import com.fpt.sealhackathon.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Get list events", description = "Filter events by keyword, status, seasonYear")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<List<EventResponse>>> eventFilter(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer seasonYear) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get events successfully",
                        eventService.eventFilter(keyword, seasonYear)));
    }

    @PostMapping
    @Operation(summary = "Create event", description = "Create new event")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<EventResponse>> create(
            @RequestBody @Valid EventRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create event successfully",
                        eventService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update event", description = "Update event by id")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<EventResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid EventRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Update event successfully",
                        eventService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete event", description = "Delete event by id")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.success("Delete event successfully", null));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change event status", description = "Update only event status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<EventResponse>> changeStatus(
            @PathVariable UUID id,
            @RequestBody @Valid EventStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Change status successfully",
                        eventService.changeStatus(id, request.getStatus())));
    }

}
