package com.fpt.sealhackathon.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    @Schema(description = "JWT access token de goi cac API protected")
    private String accessToken;
    @Schema(description = "JWT refresh token de xin cap lai access token")
    private String refreshToken;
    @Schema(description = "Loai token tra ve", example = "Bearer")
    private String tokenType;
    @Schema(description = "Thong tin tong quan cua user")
    private UserSummaryResponse user;
}
