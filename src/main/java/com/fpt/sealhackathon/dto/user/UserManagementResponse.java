package com.fpt.sealhackathon.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO trả về thông tin người dùng phục vụ các API quản trị và duyệt tài khoản.
 */
@Getter
@Builder
@Schema(description = "Thong tin user tra ve cho cac API quan tri")
public class UserManagementResponse {

    @Schema(description = "ID cua user")
    private UUID id;

    @Schema(description = "Ho va ten")
    private String fullName;

    @Schema(description = "Email dang nhap")
    private String email;

    @Schema(description = "Campus ID neu la sinh vien FPT")
    private String campusId;

    @Schema(description = "Loai sinh vien", example = "FPT")
    private String studentType;

    @Schema(description = "Trang thai hien thi cho API quan tri", example = "ACTIVE")
    private String status;

    @Schema(description = "Danh dau user guest hay khong")
    private Boolean guest;

    @Schema(description = "Danh sach role hien tai")
    private List<String> roles;

    @Schema(description = "Thoi diem tao user")
    private LocalDateTime createdAt;

    @Schema(description = "Thoi diem cap nhat user")
    private LocalDateTime updatedAt;
}
