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
public class ExternalRegisterRequest {

    @Schema(description = "Ho va ten day du", example = "Tran Van B")
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Schema(description = "Email dung de dang nhap", example = "b@gmail.com")
    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Mat khau toi thieu 6 ky tu", example = "123456")
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Schema(description = "ID truong dai hoc cua external student", example = "33333333-3333-3333-3333-333333333333")
    @NotNull(message = "University ID is required")
    private UUID universityId;
}
