package com.fpt.sealhackathon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.fpt.sealhackathon.dto.campus.CampusRequest;
import com.fpt.sealhackathon.dto.campus.CampusResponse;
import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.dto.university.UniversityRequest;
import com.fpt.sealhackathon.dto.university.UniversityResponse;
import com.fpt.sealhackathon.service.CampusService;
import com.fpt.sealhackathon.service.UniversityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
public class UniversityController {

        private final UniversityService universityService;
        private final CampusService campusService;

        @GetMapping()
        @Operation(summary = "Lay danh sach university", description = "Ho tro filter keyword")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<List<UniversityResponse>>> universityFilter(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) List<UUID> campusIds) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Get university successfully",
                                universityService.universityFilter(keyword, campusIds)));
        }

        @PostMapping
        @Operation(summary = "Create university", description = "Create university")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<UniversityResponse>> create(@RequestBody @Valid UniversityRequest request) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Create university successfully",
                                universityService.create(request)));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Updated university", description = "Updated university")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<UniversityResponse>> update(
                        @PathVariable UUID id,
                        @RequestBody @Valid UniversityRequest request) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Updated university successfully",
                                universityService.update(id, request)));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete university", description = "delete university")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public void delete(@PathVariable UUID id) {

                universityService.delete(id);
        }

        @GetMapping("/{id}/campuses")
        @Operation(summary = "Lay danh sach campus by university", description = "Ho tro filter campus by university")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<List<CampusResponse>>> campusFilter(@PathVariable UUID id) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Get campus successfully",
                                campusService.campusByUniversity(id)));
        }

        @PostMapping("/{id}/campuses")
        @Operation(summary = "Create Campus", description = "Create Campus")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
        })
        public ResponseEntity<ApiResponse<CampusResponse>> create(@PathVariable UUID id,
                        @RequestBody @Valid CampusRequest campus) {
                return ResponseEntity.ok(ApiResponse.success(
                                "Updated Campus successfully",
                                campusService.create(id, campus)));
        }
}