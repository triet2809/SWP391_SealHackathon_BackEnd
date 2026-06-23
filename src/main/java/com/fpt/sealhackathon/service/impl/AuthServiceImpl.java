package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.auth.AuthResponse;
import com.fpt.sealhackathon.dto.auth.ExternalRegisterRequest;
import com.fpt.sealhackathon.dto.auth.FptRegisterRequest;
import com.fpt.sealhackathon.dto.auth.LoginRequest;
import com.fpt.sealhackathon.dto.auth.MeResponse;
import com.fpt.sealhackathon.dto.auth.RefreshTokenRequest;
import com.fpt.sealhackathon.dto.auth.UserSummaryResponse;
import com.fpt.sealhackathon.entity.Role;
import com.fpt.sealhackathon.entity.User;
import com.fpt.sealhackathon.entity.enums.AccountStatus;
import com.fpt.sealhackathon.entity.enums.StudentType;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.exception.TokenInvalidException;
import com.fpt.sealhackathon.exception.UnauthorizedException;
import com.fpt.sealhackathon.repository.RoleRepository;
import com.fpt.sealhackathon.repository.UserRepository;
import com.fpt.sealhackathon.security.CustomUserDetailsService;
import com.fpt.sealhackathon.security.JwtService;
import com.fpt.sealhackathon.security.TokenBlacklistService;
import com.fpt.sealhackathon.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_STUDENT";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    // Flow register FPT bắt buộc có campusId và luôn tạo user với role mặc định ROLE_STUDENT.
    @Override
    @Transactional
    public UserSummaryResponse registerFpt(FptRegisterRequest request) {
        if (request.getCampusId() == null || request.getCampusId().isBlank()) {
            throw new IllegalArgumentException("Campus ID is required for FPT registration");
        }

        User user = buildUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getCampusId(),
                StudentType.fpt
        );

        return buildUserSummary(userRepository.save(user));
    }

    // Flow register External dùng chung logic tạo user nhưng bỏ trống campusId.
    @Override
    @Transactional
    public UserSummaryResponse registerExternal(ExternalRegisterRequest request) {
        User user = buildUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                null,
                StudentType.external
        );

        return buildUserSummary(userRepository.save(user));
    }

    // Login xác thực email/password rồi phát hành cặp access token và refresh token cho client.
    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (user.getStatus() != AccountStatus.approved) {
            throw new UnauthorizedException("User account is inactive");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        return buildAuthResponse(user, userDetails);
    }

    // Refresh token chỉ dùng để xin token mới, không cho phép thay access token ở API business.
    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
            throw new TokenInvalidException("Refresh token is invalid or expired");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        return buildAuthResponse(user, userDetails);
    }

    // Logout theo MVP sẽ blacklist access token hiện tại để các request sau bị chặn ngay.
    @Override
    public void logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);

        String username = jwtService.extractUsername(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        if (!jwtService.isAccessTokenValid(token, userDetails)) {
            throw new TokenInvalidException("Access token is invalid or expired");
        }

        tokenBlacklistService.blacklistToken(token);
    }

    // /auth/me lấy email từ Authentication do JwtAuthenticationFilter đã gắn vào SecurityContext.
    @Override
    public MeResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + authentication.getName()
                ));

        return MeResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .campusId(user.getCampusId())
                .studentType(user.getStudentType().name().toUpperCase(Locale.ROOT))
                .status(toApiStatus(user.getStatus()))
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // Gom phần validate duplicate, hash password và gán role để hai flow register không bị lặp code.
    private User buildUser(
            String fullName,
            String email,
            String rawPassword,
            String campusId,
            StudentType studentType
    ) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        return User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .campusId(campusId)
                .studentType(studentType)
                .status(AccountStatus.approved)
                .isGuest(false)
                .roles(Set.of(getOrCreateStudentRole()))
                .build();
    }

    // Nếu role mặc định chưa có trong DB thì tự tạo để flow demo/test không bị phụ thuộc seed thủ công.
    private Role getOrCreateStudentRole() {
        return roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(Role.builder().name(DEFAULT_ROLE).build()));
    }

    // Tạo response login/refresh theo cùng một format để frontend xử lý thống nhất.
    private AuthResponse buildAuthResponse(User user, UserDetails userDetails) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(userDetails))
                .refreshToken(jwtService.generateRefreshToken(userDetails))
                .tokenType("Bearer")
                .user(buildUserSummary(user))
                .build();
    }

    private UserSummaryResponse buildUserSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .campusId(user.getCampusId())
                .studentType(user.getStudentType().name().toUpperCase(Locale.ROOT))
                .status(toApiStatus(user.getStatus()))
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header must start with Bearer");
        }

        return authorizationHeader.substring(7);
    }

    private String toApiStatus(AccountStatus status) {
        return status == AccountStatus.approved ? "ACTIVE" : "INACTIVE";
    }
}
