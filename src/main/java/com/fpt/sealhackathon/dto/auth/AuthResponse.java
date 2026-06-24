package com.fpt.sealhackathon.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO trả về cặp token và thông tin tóm tắt của người dùng sau đăng nhập hoặc làm mới token.
 */
@Getter
@Builder
@Schema(description = "Thong tin token va tong quan nguoi dung sau khi xac thuc thanh cong")
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
