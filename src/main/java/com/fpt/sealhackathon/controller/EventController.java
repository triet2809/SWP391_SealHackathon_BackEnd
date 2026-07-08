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
                        @RequestParam(required = false) Integer seasonYear,
                        @RequestParam(required = false) EventStatus status) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Get events successfully",
                                                eventService.eventFilter(keyword, seasonYear, status)));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get event", description = "Filter event by id")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<EventResponse>> eventById(@PathVariable UUID id) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Get event successfully",
                                                eventService.eventById(id)));
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

        @PatchMapping("/{id}")
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

        @PatchMapping("/{id}/publish")
        @Operation(summary = "Change event status published", description = "Change event status published")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<EventResponse>> changePublish(@PathVariable UUID id) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Change status published successfully",
                                                eventService.changeStatus(id, EventStatus.published)));
        }

        @PatchMapping("/{id}/open-registration")
        @Operation(summary = "Change event status open registration", description = "Change event status open registration")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<EventResponse>> changeOpenRegistration(@PathVariable UUID id) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Change status open registration successfully",
                                                eventService.changeStatus(id, EventStatus.registration_open)));
        }

        @PatchMapping("/{id}/close-registration")
        @Operation(summary = "Change event status close registration", description = "Change event status close registration")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<EventResponse>> changeCloseRegistration(@PathVariable UUID id) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Change status close registration successfully",
                                                eventService.changeStatus(id, EventStatus.registration_closed)));
        }

        @PatchMapping("/{id}/start")
        @Operation(summary = "Change event status start", description = "Change event status start")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<EventResponse>> changeStart(@PathVariable UUID id) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Change status start successfully",
                                                eventService.changeStatus(id, EventStatus.ongoing)));
        }

        @PatchMapping("/{id}/complete")
        @Operation(summary = "Change event status complete", description = "Change event status complete")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<EventResponse>> changeComplete(@PathVariable UUID id) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Change status complete successfully",
                                                eventService.changeStatus(id, EventStatus.completed)));
        }

}
