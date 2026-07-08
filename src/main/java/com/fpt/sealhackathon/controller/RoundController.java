package com.fpt.sealhackathon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackRequest;
import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackResponse;
import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.dto.round.RoundResponse;
import com.fpt.sealhackathon.dto.round.RoundUpsertRequest;
import com.fpt.sealhackathon.entity.enums.RoundStatus;
import com.fpt.sealhackathon.service.RoundService;
import com.fpt.sealhackathon.service.RoundTrackService;

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
        private final RoundTrackService roundTrackService;

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

        @GetMapping("/{id}")
        @Operation(summary = "Get round", description = "Filter by roundId")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundResponse>> roundFilter(@PathVariable UUID id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Get round by id successfully",
                                roundService.rounndById(id)));
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
                        @RequestBody @Valid RoundUpsertRequest request) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Update round successfully",
                                roundService.update(id, request)));
        }

        @PatchMapping("/{id}")
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

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete round", description = "Delete round by id")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundResponse>> deleteRound(@PathVariable UUID id) {
                roundService.delete(id);
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Delete round successfully",
                                                null));
        }

        @PatchMapping("/{id}/open")
        @Operation(summary = "Change round status open", description = "Update round status open")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundResponse>> changeOpen(@PathVariable UUID id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Change round status open successfully",
                                roundService.changeStatus(id, RoundStatus.open)));
        }

        @PatchMapping("/{id}/close-submission")
        @Operation(summary = "Change round status close submission", description = "Update round status close submission")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundResponse>> changeCloseSubmission(
                        @PathVariable UUID id,
                        @RequestParam RoundStatus status) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Change round status close submission successfully",
                                roundService.changeStatus(id, RoundStatus.submission_closed)));
        }

        @PatchMapping("/{id}/start-scoring")
        @Operation(summary = "Change round status start scoring", description = "Update round status start scoring")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundResponse>> changeStartScoring(@PathVariable UUID id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Change round status start scoring successfully",
                                roundService.changeStatus(id, RoundStatus.scoring)));
        }

        @PatchMapping("/{id}/publish-ranking")
        @Operation(summary = "Change round status publish ranking", description = "Update round status publish ranking")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundResponse>> changePublishRanking(@PathVariable UUID id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Change round status successfully",
                                roundService.changeStatus(id, RoundStatus.ranking_published)));
        }

        @PatchMapping("/{id}/complete")
        @Operation(summary = "Change round status complete", description = "Update round status complete")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundResponse>> changeComplete(@PathVariable UUID id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Change round status completed successfully",
                                roundService.changeStatus(id, RoundStatus.completed)));
        }

        @GetMapping("/{id}/tracks")
        @Operation(summary = "Get round track list", description = "Filter by round Id")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<List<RoundTrackResponse>>> getTrackByRoundId(@PathVariable UUID id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Get round tracks successfully",
                                roundTrackService.roundTrackByTrack(id)));
        }

        @PostMapping("/{id}/tracks")
        @Operation(summary = "Create round track ", description = "Create round track")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<RoundTrackResponse>> create(
                        @PathVariable UUID id,
                        @RequestBody @Valid RoundTrackRequest request) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Create round track successfully",
                                roundTrackService.create(id, request)));
        }
}
