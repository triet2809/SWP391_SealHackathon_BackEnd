package vn.edu.fpt.seal.modules.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.AccountStatus;
import vn.edu.fpt.seal.common.enums.RoleName;
import vn.edu.fpt.seal.common.enums.StudentType;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.auth.dto.AuthResponse;
import vn.edu.fpt.seal.modules.auth.dto.LoginRequest;
import vn.edu.fpt.seal.modules.auth.dto.RefreshRequest;
import vn.edu.fpt.seal.modules.auth.dto.RegisterRequest;
import vn.edu.fpt.seal.modules.university.entity.Campus;
import vn.edu.fpt.seal.modules.university.entity.University;
import vn.edu.fpt.seal.modules.university.repository.CampusRepository;
import vn.edu.fpt.seal.modules.university.repository.UniversityRepository;
import vn.edu.fpt.seal.modules.user.entity.Role;
import vn.edu.fpt.seal.modules.user.entity.User;
import vn.edu.fpt.seal.modules.user.repository.RoleRepository;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;
import vn.edu.fpt.seal.security.JwtService;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CampusRepository campusRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email().toLowerCase().trim())) {
            throw ApiException.conflict("Email already registered");
        }

        StudentType type = req.studentType() != null ? req.studentType() : StudentType.none;
        Campus campus = null;
        University university = null;

        if (req.campusId() != null) {
            campus = campusRepository.findWithUniversityById(req.campusId())
                    .orElseThrow(() -> ApiException.notFound("Campus not found"));
            university = campus.getUniversity();
        } else if (req.universityId() != null) {
            university = universityRepository.findById(req.universityId())
                    .orElseThrow(() -> ApiException.notFound("University not found"));
        } else if (type == StudentType.external) {
            String universityName = req.universityName() == null ? null : req.universityName().trim();
            if (universityName == null || universityName.isBlank()) {
                throw ApiException.badRequest("University name is required for external students");
            }
            university = universityRepository.findByNameIgnoreCase(universityName)
                    .orElseGet(() -> universityRepository.save(University.builder()
                            .name(universityName)
                            .country("Vietnam")
                            .build()));
        }

        Role defaultRole = roleRepository.findByName(RoleName.TEAM_MEMBER)
                .orElseThrow(() -> new IllegalStateException("Default role team_member not seeded"));

        User user = User.builder()
                .email(req.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName().trim())
                .studentType(type)
                .studentId(req.studentId())
                .university(university)
                .campus(campus)
                .isGuest(false)
                .status(AccountStatus.pending)
                .roles(new HashSet<>(List.of(defaultRole)))
                .build();

        user = userRepository.save(user);
        log.info("User registered (pending approval): {}", user.getEmail());

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid email or password");
        }

        if (user.getStatus() == AccountStatus.rejected) {
            throw ApiException.forbidden("Your account has been rejected");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String authorizationHeader, String refreshToken) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                jwtService.revokeToken(token);
            } catch (Exception e) {
                throw ApiException.unauthorized("Invalid access token");
            }
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                jwtService.revokeToken(refreshToken);
            } catch (Exception e) {
                throw ApiException.unauthorized("Invalid refresh token");
            }
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest req) {
        Claims claims;
        try {
            claims = jwtService.parseToken(req.refreshToken());
        } catch (Exception e) {
            throw ApiException.unauthorized("Invalid refresh token");
        }
        if (!jwtService.isRefreshToken(claims)) {
            throw ApiException.unauthorized("Not a refresh token");
        }
        if (jwtService.isRevoked(req.refreshToken())) {
            throw ApiException.unauthorized("Refresh token has been revoked");
        }
        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        List<String> roleNames = user.getRoles().stream().map(Role::getName).toList();
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), roleNames);
        String refresh = jwtService.generateRefreshToken(user.getId());
        Campus campus = user.getCampus();
        University university = campus != null ? campus.getUniversity() : user.getUniversity();

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .status(user.getStatus().name())
                        .studentType(user.getStudentType().name())
                        .universityId(university == null ? null : university.getId())
                        .universityName(university == null ? null : university.getName())
                        .campusId(campus == null ? null : campus.getId())
                        .campusName(campus == null ? null : campus.getName())
                        .isGuest(user.isGuest())
                        .roles(roleNames)
                        .build())
                .build();
    }
}
