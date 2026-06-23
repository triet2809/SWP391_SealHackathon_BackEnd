package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.dto.role.RoleResponse;
import com.fpt.sealhackathon.dto.user.UserManagementResponse;
import com.fpt.sealhackathon.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Cac API quan tri role danh cho coordinator")
public class RoleController {

    private final RoleService roleService;

    // Tra ve danh sach role tu DB de giao dien admin co the bind dropdown phan quyen dong.
    @GetMapping
    @Operation(summary = "Lay danh sach role", description = "Doc toan bo role hien co trong he thong")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay role thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content)
    })
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.success("Get roles successfully", roleService.getRoles()));
    }

    // Assign role cho user la thao tac tren bang user_roles; service se chan duplicate truoc khi save.
    @PostMapping("/{roleId}/users/{userId}")
    @Operation(summary = "Gan role cho user", description = "Them mot role cho user neu user chua co role do")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gan role thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khong tim thay user hoac role", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User da co role nay", content = @Content)
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> assignRole(
            @PathVariable UUID roleId,
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Assign role successfully",
                roleService.assignRole(roleId, userId)
        ));
    }

    // Remove role can bao loi ro rang neu user chua duoc gan role do truoc day.
    @DeleteMapping("/{roleId}/users/{userId}")
    @Operation(summary = "Xoa role khoi user", description = "Go bo role da duoc gan cho user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xoa role thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Khong tim thay user, role hoac role assignment", content = @Content)
    })
    public ResponseEntity<ApiResponse<UserManagementResponse>> removeRole(
            @PathVariable UUID roleId,
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Remove role successfully",
                roleService.removeRole(roleId, userId)
        ));
    }
}
