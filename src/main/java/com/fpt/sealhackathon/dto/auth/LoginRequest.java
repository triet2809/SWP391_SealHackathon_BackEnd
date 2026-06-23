package com.fpt.sealhackathon.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @Schema(description = "Email dang nhap", example = "a@fpt.edu.vn")
    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Mat khau dang nhap", example = "123456")
    @NotBlank(message = "Password is required")
    private String password;
}
