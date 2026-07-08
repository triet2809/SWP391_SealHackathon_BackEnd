package com.fpt.sealhackathon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackRequest;
import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackResponse;
import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.service.RoundTrackService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/round-tracks")
@RequiredArgsConstructor
public class RoundTrackController {

        private final RoundTrackService roundTrackService;

        @GetMapping
        @Operation(summary = "Get round track list", description = "Filter by round track and keyword")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<List<RoundTrackResponse>>> filter(
                        @RequestParam(required = false) UUID eventId,
                        @RequestParam(required = false) UUID roundId,
                        @RequestParam(required = false) String keyword) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Get round tracks successfully",
                                roundTrackService.roundTrackFilter(
                                                eventId,
                                                roundId,
                                                keyword)));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get round track", description = "Filter by track Id")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundTrackResponse>> getTrackById(@PathVariable UUID id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Get round track successfully",
                                roundTrackService.roundTrackById(id)));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Updated round track ", description = "Updated round track ")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundTrackResponse>> update(
                        @PathVariable UUID id,
                        @RequestBody @Valid RoundTrackRequest request) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Update round track successfully",
                                roundTrackService.update(id, request)));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete round track ", description = "Delete round track ")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {

                roundTrackService.delete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Delete round track successfully",
                                                null));
        }

        @PatchMapping("/{id}/promotion-rule")
        @Operation(summary = "Update promotion rule", description = "Update the number of teams promoted to the next round")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundTrackResponse>> updatePromoted(
                        @PathVariable UUID id,
                        @RequestParam Integer topNToPromote) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Update promotion rule successfully",
                                                roundTrackService.updatePromotionRule(id, topNToPromote)));
        }
}
