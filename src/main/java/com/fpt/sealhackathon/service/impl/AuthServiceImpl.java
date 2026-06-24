package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.auth.AuthResponse;
import com.fpt.sealhackathon.dto.auth.ExternalRegisterRequest;
import com.fpt.sealhackathon.dto.auth.FptRegisterRequest;
import com.fpt.sealhackathon.dto.auth.GuestJudgeRegisterRequest;
import com.fpt.sealhackathon.dto.auth.LoginRequest;
import com.fpt.sealhackathon.dto.auth.MeResponse;
import com.fpt.sealhackathon.dto.auth.RefreshTokenRequest;
import com.fpt.sealhackathon.dto.auth.UserSummaryResponse;
import com.fpt.sealhackathon.entity.Role;
import com.fpt.sealhackathon.entity.User;
import com.fpt.sealhackathon.entity.enums.AccountStatus;
import com.fpt.sealhackathon.entity.enums.StudentType;
import com.fpt.sealhackathon.exception.DuplicateEmailException;
import com.fpt.sealhackathon.exception.ForbiddenException;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Service xu ly cac use case auth, register, refresh token va thong tin current user.
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_STUDENT";
    private static final List<String> COORDINATOR_AUTHORITIES = List.of("coordinator", "COORDINATOR", "ROLE_COORDINATOR");
    private static final List<String> JUDGE_ROLE_CANDIDATES = List.of("judge", "JUDGE", "ROLE_JUDGE");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    // Flow register FPT bat buoc co university, campus va studentId, sau do tao user voi role mac dinh.
    @Override
    @Transactional
    public UserSummaryResponse registerFpt(FptRegisterRequest request) {
        if (request.getUniversityId() == null) {
            throw new IllegalArgumentException("University ID is required for FPT registration");
        }

        if (request.getCampusId() == null) {
            throw new IllegalArgumentException("Campus ID is required for FPT registration");
        }

        if (request.getStudentId() == null || request.getStudentId().isBlank()) {
            throw new IllegalArgumentException("Student ID is required for FPT registration");
        }

        User user = buildUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getUniversityId(),
                request.getCampusId(),
                request.getStudentId(),
                StudentType.fpt
        );

        return buildUserSummary(userRepository.save(user));
    }

    // Flow register External dung chung logic tao user nhung bo trong campusId va studentId.
    @Override
    @Transactional
    public UserSummaryResponse registerExternal(ExternalRegisterRequest request) {
        if (request.getUniversityId() == null) {
            throw new IllegalArgumentException("University ID is required for external registration");
        }

        User user = buildUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getUniversityId(),
                null,
                null,
                StudentType.external
        );

        return buildUserSummary(userRepository.save(user));
    }

    // Flow guest judge duoc coordinator kich hoat truc tiep va gan role judge ngay khi tao tai khoan.
    @Override
    @Transactional
    public UserSummaryResponse createGuestJudge(GuestJudgeRegisterRequest request, Authentication authentication) {
        validateCoordinatorAuthority(authentication);

        User user = buildUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                null,
                null,
                null,
                StudentType.none,
                true,
                getOrCreateRole(JUDGE_ROLE_CANDIDATES, "judge")
        );

        return buildUserSummary(userRepository.save(user));
    }

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

    // Refresh token chi dung de xin token moi, khong cho phep thay access token o API business.
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

    // Logout theo MVP se blacklist access token hien tai de cac request sau bi chan ngay.
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

    // /auth/me lay email tu Authentication do JwtAuthenticationFilter da gan vao SecurityContext.
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
                .studentId(user.getStudentId())
                .studentType(user.getStudentType().name().toUpperCase(Locale.ROOT))
                .status(toApiStatus(user.getStatus()))
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // Gom phan validate duplicate, hash password va gan role de cac flow register khong bi lap code.
    private User buildUser(
            String fullName,
            String email,
            String rawPassword,
            UUID universityId,
            UUID campusId,
            String studentId,
            StudentType studentType
    ) {
        return buildUser(
                fullName,
                email,
                rawPassword,
                universityId,
                campusId,
                studentId,
                studentType,
                false,
                getOrCreateStudentRole()
        );
    }

    private User buildUser(
            String fullName,
            String email,
            String rawPassword,
            UUID universityId,
            UUID campusId,
            String studentId,
            StudentType studentType,
            boolean isGuest,
            Role role
    ) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already exists");
        }

        return User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .universityId(universityId)
                .campusIdRef(campusId)
                .studentId(studentId)
                .studentType(studentType)
                .status(AccountStatus.approved)
                .isGuest(isGuest)
                .roles(Set.of(role))
                .build();
    }

    // Neu role mac dinh chua co trong DB thi tu tao de flow demo/test khong bi phu thuoc seed thu cong.
    private Role getOrCreateStudentRole() {
        return getOrCreateRole(List.of(DEFAULT_ROLE), DEFAULT_ROLE);
    }

    private Role getOrCreateRole(List<String> candidates, String fallbackName) {
        return candidates.stream()
                .map(roleRepository::findByNameIgnoreCase)
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(() -> roleRepository.save(Role.builder().name(fallbackName).build()));
    }

    private void validateCoordinatorAuthority(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        boolean isCoordinator = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .anyMatch(COORDINATOR_AUTHORITIES::contains);

        if (!isCoordinator) {
            throw new ForbiddenException("User does not have coordinator permission");
        }
    }

    // Tao response login/refresh theo cung mot format de frontend xu ly thong nhat.
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
                .studentId(user.getStudentId())
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
