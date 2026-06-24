package com.fpt.sealhackathon.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
// DTO tong hop thong tin user duoc tra ve trong cac flow auth.
public class UserSummaryResponse {

    @Schema(description = "ID cua user")
    private UUID id;
    @Schema(description = "Ho va ten")
    private String fullName;
    @Schema(description = "Email dang nhap")
    private String email;
    @Schema(description = "Campus ID UUID tra ve cho flow FPT")
    private String campusId;
    @Schema(description = "Ma sinh vien cua user neu co", example = "SE123456")
    private String studentId;
    @Schema(description = "Loai sinh vien", example = "FPT")
    private String studentType;
    @Schema(description = "Trang thai hien thi ra API", example = "ACTIVE")
    private String status;
    @Schema(description = "Danh sach role cua user")
    private List<String> roles;
}
