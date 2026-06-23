package com.fpt.sealhackathon.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {

    @Schema(description = "Refresh token duoc cap sau khi login", example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
