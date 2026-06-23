package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.auth.AuthResponse;
import com.fpt.sealhackathon.dto.auth.ExternalRegisterRequest;
import com.fpt.sealhackathon.dto.auth.FptRegisterRequest;
import com.fpt.sealhackathon.dto.auth.LoginRequest;
import com.fpt.sealhackathon.dto.auth.MeResponse;
import com.fpt.sealhackathon.dto.auth.RefreshTokenRequest;
import com.fpt.sealhackathon.dto.auth.UserSummaryResponse;
import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Đăng ký tài khoản sinh viên FPT; sau khi validate request sẽ tạo user và role mặc định.
    @PostMapping("/register/fpt")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> registerFpt(
            @Valid @RequestBody FptRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Register FPT successfully", authService.registerFpt(request)));
    }

    // Đăng ký tài khoản external student với cùng flow nhưng không yêu cầu campusId.
    @PostMapping("/register/external")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> registerExternal(
            @Valid @RequestBody ExternalRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Register external successfully", authService.registerExternal(request)));
    }

    // Login nhận email/password và trả về access token, refresh token cùng thông tin user.
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successfully", authService.login(request)));
    }

    // Endpoint này dùng refresh token hợp lệ để xin cặp token mới mà không cần nhập lại mật khẩu.
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Refresh token successfully",
                authService.refreshToken(request)
        ));
    }

    // Logout lấy access token từ header Authorization và đưa token đó vào blacklist.
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        authService.logout(authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success("Logout successfully", null));
    }

    // /auth/me không đọc body hay path param; nó dựa hoàn toàn vào Authentication hiện tại.
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Get current user successfully",
                authService.getCurrentUser(authentication)
        ));
    }
}
