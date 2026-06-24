package com.fpt.sealhackathon.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FptRegisterRequest {

    @Schema(description = "Ho va ten day du", example = "Nguyen Van A")
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Schema(description = "Email dung de dang nhap", example = "a@fpt.edu.vn")
    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Mat khau toi thieu 6 ky tu", example = "123456")
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Schema(description = "University ID cua sinh vien FPT", example = "11111111-1111-1111-1111-111111111111")
    @NotNull(message = "University ID is required")
    private UUID universityId;

    @Schema(description = "Campus ID cua sinh vien FPT", example = "22222222-2222-2222-2222-222222222221")
    @NotNull(message = "Campus ID is required")
    private UUID campusId;

    @Schema(description = "Ma sinh vien FPT", example = "SE123456")
    @NotBlank(message = "Student ID is required")
    private String studentId;
}
