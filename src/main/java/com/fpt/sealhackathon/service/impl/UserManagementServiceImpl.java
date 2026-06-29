package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.user.UserManagementResponse;
import com.fpt.sealhackathon.dto.user.UserStatusUpdateRequest;
import com.fpt.sealhackathon.entity.AuditLog;
import com.fpt.sealhackathon.entity.Role;
import com.fpt.sealhackathon.entity.User;
import com.fpt.sealhackathon.entity.enums.AccountStatus;
import com.fpt.sealhackathon.entity.enums.AuditAction;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.exception.UnauthorizedException;
import com.fpt.sealhackathon.repository.AuditLogRepository;
import com.fpt.sealhackathon.repository.UserRepository;
import com.fpt.sealhackathon.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Hiện thực các chức năng quản trị người dùng, duyệt tài khoản và ghi nhận audit log.
 */
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    // API listing trung tam cho man hinh coordinator; no gom filter role, status va search tren ten/email/campusId.
    @Override
    @Transactional(readOnly = true)
    public List<UserManagementResponse> getUsers(String role, String status, String search) {
        AccountStatus accountStatus = parseStatus(status);
        Specification<User> specification = buildUserSpecification(normalize(role), accountStatus, normalize(search));

        return userRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::toResponse)
                .toList();
    }

    // Chi tiet user duoc doc theo id va tra ve kem danh sach role de trang admin khong phai goi them API phu.
    @Override
    @Transactional(readOnly = true)
    public UserManagementResponse getUserById(UUID userId) {
        return toResponse(getUserOrThrow(userId));
    }

    // Pending users la danh sach cho coordinator duyet; hien tai schema khong co approved flag rieng nen dua vao status.
    @Override
    @Transactional(readOnly = true)
    public List<UserManagementResponse> getPendingUsers() {
        return userRepository.findAllByStatusOrderByCreatedAtDesc(AccountStatus.pending).stream()
                .map(this::toResponse)
                .toList();
    }

    // Approve user chuyen status sang approved va ghi audit log de sau nay truy vet ai da duyet.
    @Override
    @Transactional
    public UserManagementResponse approveUser(UUID userId, Authentication authentication) {
        User targetUser = getUserOrThrow(userId);
        User actor = getActor(authentication);
        AccountStatus oldStatus = targetUser.getStatus();

        targetUser.setStatus(AccountStatus.approved);
        User savedUser = userRepository.save(targetUser);

        createAuditLog(actor, targetUser, AuditAction.APPROVE_USER, oldStatus, AccountStatus.approved,
                "Coordinator approved user account");
        return toResponse(savedUser);
    }

    // Reject user cap nhat trang thai va de lai audit log de phan rejection minh bach hon.
    @Override
    @Transactional
    public UserManagementResponse rejectUser(UUID userId, Authentication authentication) {
        User targetUser = getUserOrThrow(userId);
        User actor = getActor(authentication);
        AccountStatus oldStatus = targetUser.getStatus();

        targetUser.setStatus(AccountStatus.rejected);
        User savedUser = userRepository.save(targetUser);

        createAuditLog(actor, targetUser, AuditAction.REJECT_USER, oldStatus, AccountStatus.rejected,
                "Coordinator rejected user account");
        return toResponse(savedUser);
    }

    // PATCH status la endpoint tong quat cho admin; no cho phep doi trang thai ma van tai su dung chung mapper/log.
    @Override
    @Transactional
    public UserManagementResponse updateStatus(
            UUID userId,
            UserStatusUpdateRequest request,
            Authentication authentication
    ) {
        User targetUser = getUserOrThrow(userId);
        User actor = getActor(authentication);
        AccountStatus newStatus = parseStatus(request.getStatus());
        AccountStatus oldStatus = targetUser.getStatus();

        targetUser.setStatus(newStatus);
        User savedUser = userRepository.save(targetUser);

        createAuditLog(actor, targetUser, AuditAction.UPDATE, oldStatus, newStatus,
                "Coordinator updated user status");
        return toResponse(savedUser);
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private User getActor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coordinator not found with email: " + authentication.getName()
                ));
    }

    private void createAuditLog(
            User actor,
            User targetUser,
            AuditAction action,
            AccountStatus oldStatus,
            AccountStatus newStatus,
            String details
    ) {
        // Cast action sang audit_action ngay trong SQL de PostgreSQL luu dung enum thay vi varchar.
        jdbcTemplate.update(
                """
                insert into audit_logs (
                    id, user_id, action, target_type, target_id, old_value, new_value, details, occurred_at
                ) values (
                    ?, ?, cast(? as audit_action), ?, ?, ?, ?, ?, ?
                )
                """,
                UUID.randomUUID(),
                actor.getId(),
                action.name(),
                "USER",
                targetUser.getId(),
                oldStatus == null ? null : oldStatus.name(),
                newStatus == null ? null : newStatus.name(),
                details,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    // Dung Specification de PostgreSQL nhan dien dung kieu String cho search filter va tranh loi lower(bytea).
    private Specification<User> buildUserSpecification(String role, AccountStatus status, String search) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            if (role != null) {
                Join<User, Role> roleJoin = root.join("roles", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(roleJoin.get("name")), role.toLowerCase(Locale.ROOT)));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (search != null) {
                String keyword = "%" + search.toLowerCase(Locale.ROOT) + "%";
                List<Predicate> searchPredicates = new ArrayList<>();
                // lower() chi dung cho cac field String; neu search la UUID hop le thi parse UUID va dung equal() cho id.
                searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("fullName"), "")), keyword));
                searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("email"), "")), keyword));
                searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("campusId"), "")), keyword));

                try {
                    UUID parsedUuid = UUID.fromString(search);
                    searchPredicates.add(criteriaBuilder.equal(root.get("id"), parsedUuid));
                } catch (IllegalArgumentException ignored) {
                    // Search khong phai UUID hop le thi bo qua predicate id.
                }

                predicates.add(criteriaBuilder.or(searchPredicates.toArray(Predicate[]::new)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private UserManagementResponse toResponse(User user) {
        return UserManagementResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .campusId(user.getCampusId())
                .studentType(user.getStudentType().name().toUpperCase(Locale.ROOT))
                .status(toApiStatus(user.getStatus()))
                .guest(user.getIsGuest())
                .roles(user.getRoles().stream().map(Role::getName).sorted().toList())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private AccountStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "ACTIVE", "APPROVED" -> AccountStatus.approved;
            case "PENDING" -> AccountStatus.pending;
            case "REJECTED" -> AccountStatus.rejected;
            default -> throw new IllegalArgumentException("Unsupported status: " + status);
        };
    }

    private String toApiStatus(AccountStatus status) {
        return switch (status) {
            case approved -> "ACTIVE";
            case pending -> "PENDING";
            case rejected -> "REJECTED";
        };
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
