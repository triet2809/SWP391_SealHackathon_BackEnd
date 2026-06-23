package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.dto.user.UserManagementResponse;
import com.fpt.sealhackathon.dto.user.UserStatusUpdateRequest;
import com.fpt.sealhackathon.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Cac API quan tri user danh cho coordinator")
public class UserManagementController {

    private final UserManagementService userManagementService;

    // Endpoint listing cho phep gom filter ngay tu query string de man hinh admin de dang phan trang/tim kiem.
    @GetMapping
    @Operation(summary = "Lay danh sach user", description = "Ho tro filter theo role, status va search keyword")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<List<UserManagementResponse>>> getUsers(
            @Parameter(description = "Loc theo ten role", example = "team_member")
            @RequestParam(required = false) String role,
            @Parameter(description = "Loc theo status", example = "ACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Tu khoa tim trong ten, email, campusId", example = "student01")
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Get users successfully",
                userManagementService.getUsers(role, status, search)
        ));
    }

    // User detail tra ve thong tin day du hon listing de frontend mo drawer/modal chi tiet ma khong can tu xu ly role.
    @GetMapping("/{userId:[0-9a-fA-F\\-]{36}}")
    @Operation(summary = "Lay chi tiet user", description = "Tim mot user theo userId")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay chi tiet thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khong tim thay user", content = @Content)
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Get user detail successfully",
                userManagementService.getUserById(userId)
        ));
    }

    // Pending endpoint tach rieng de coordinator xem nhanh danh sach can duyet ma khong phai truyen filter thu cong.
    @GetMapping("/pending")
    @Operation(summary = "Lay danh sach user pending", description = "Tra ve cac user dang cho duyet")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay pending users thanh cong")
    })
    public ResponseEntity<ApiResponse<List<UserManagementResponse>>> getPendingUsers() {
        return ResponseEntity.ok(ApiResponse.success(
                "Get pending users successfully",
                userManagementService.getPendingUsers()
        ));
    }

    // Approve route su dung Authentication de xac dinh coordinator thao tac va ghi audit log dung actor.
    @PatchMapping("/{userId:[0-9a-fA-F\\-]{36}}/approve")
    @Operation(summary = "Approve user", description = "Duyet user va chuyen trang thai sang ACTIVE")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approve thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khong tim thay user", content = @Content)
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> approveUser(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Approve user successfully",
                userManagementService.approveUser(userId, authentication)
        ));
    }

    // Reject route giu cung pattern voi approve de frontend de su dung va backend de audit nhat quan.
    @PatchMapping("/{userId:[0-9a-fA-F\\-]{36}}/reject")
    @Operation(summary = "Reject user", description = "Tu choi user va chuyen trang thai sang REJECTED")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reject thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khong tim thay user", content = @Content)
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> rejectUser(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Reject user successfully",
                userManagementService.rejectUser(userId, authentication)
        ));
    }

    // PATCH status linh hoat hon approve/reject khi coordinator can dua user ve pending hoac cap nhat thu cong.
    @PatchMapping("/{userId:[0-9a-fA-F\\-]{36}}/status")
    @Operation(summary = "Cap nhat status user", description = "Doi status user sang ACTIVE, PENDING hoac REJECTED")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cap nhat thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Status khong hop le", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khong tim thay user", content = @Content)
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UserStatusUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Update user status successfully",
                userManagementService.updateStatus(userId, request, authentication)
        ));
    }
}
