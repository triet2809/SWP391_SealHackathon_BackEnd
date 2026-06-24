package com.fpt.sealhackathon.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO trả về thông tin chi tiết của người dùng hiện tại sau khi đã xác thực JWT.
 */
@Getter
@Builder
@Schema(description = "Thong tin chi tiet cua nguoi dung hien tai")
public class MeResponse {

    @Schema(description = "ID cua user")
    private UUID id;
    @Schema(description = "Ho va ten")
    private String fullName;
    @Schema(description = "Email dang nhap")
    private String email;
    @Schema(description = "Campus ID tra ve cho flow FPT")
    private String campusId;
    @Schema(description = "Loai sinh vien", example = "FPT")
    private String studentType;
    @Schema(description = "Trang thai hien thi ra API", example = "ACTIVE")
    private String status;
    @Schema(description = "Danh sach role cua user")
    private List<String> roles;
    @Schema(description = "Thoi diem tao tai khoan")
    private LocalDateTime createdAt;
    @Schema(description = "Thoi diem cap nhat gan nhat")
    private LocalDateTime updatedAt;
}
