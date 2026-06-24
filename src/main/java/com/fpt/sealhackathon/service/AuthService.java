package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.auth.AuthResponse;
import com.fpt.sealhackathon.dto.auth.ExternalRegisterRequest;
import com.fpt.sealhackathon.dto.auth.FptRegisterRequest;
import com.fpt.sealhackathon.dto.auth.LoginRequest;
import com.fpt.sealhackathon.dto.auth.MeResponse;
import com.fpt.sealhackathon.dto.auth.RefreshTokenRequest;
import com.fpt.sealhackathon.dto.auth.UserSummaryResponse;
import org.springframework.security.core.Authentication;

/**
 * Khai báo các chức năng xác thực, đăng ký và truy xuất thông tin người dùng hiện tại.
 */
public interface AuthService {

    UserSummaryResponse registerFpt(FptRegisterRequest request);

    UserSummaryResponse registerExternal(ExternalRegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String authorizationHeader);

    MeResponse getCurrentUser(Authentication authentication);
}
