package com.fpt.sealhackathon.service.impl;

import com.fpt.sealhackathon.dto.role.RoleResponse;
import com.fpt.sealhackathon.dto.user.UserManagementResponse;
import com.fpt.sealhackathon.entity.Role;
import com.fpt.sealhackathon.entity.User;
import com.fpt.sealhackathon.exception.ConflictException;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.repository.RoleRepository;
import com.fpt.sealhackathon.repository.UserRepository;
import com.fpt.sealhackathon.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    // Role listing giup frontend lay danh muc role hien co tu DB thay vi hard-code o client.
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAllByOrderByNameAsc().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .toList();
    }

    // Assign role kiem tra ca hai dau: role phai ton tai va user chua co role do, tranh duplicate trong user_roles.
    @Override
    @Transactional
    public UserManagementResponse assignRole(UUID roleId, UUID userId) {
        User user = getUserOrThrow(userId);
        Role role = getRoleOrThrow(roleId);

        if (user.getRoles().stream().anyMatch(existingRole -> existingRole.getId().equals(roleId))) {
            throw new ConflictException("User already has role: " + role.getName());
        }

        user.getRoles().add(role);
        return toResponse(userRepository.save(user));
    }

    // Remove role cung validate nguoc lai: neu user khong co role thi tra loi ro rang de frontend xu ly.
    @Override
    @Transactional
    public UserManagementResponse removeRole(UUID roleId, UUID userId) {
        User user = getUserOrThrow(userId);
        Role role = getRoleOrThrow(roleId);

        boolean removed = user.getRoles().removeIf(existingRole -> existingRole.getId().equals(roleId));
        if (!removed) {
            throw new ResourceNotFoundException("User does not have role: " + role.getName());
        }

        return toResponse(userRepository.save(user));
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Role getRoleOrThrow(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
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

    private String toApiStatus(com.fpt.sealhackathon.entity.enums.AccountStatus status) {
        return switch (status) {
            case approved -> "ACTIVE";
            case pending -> "PENDING";
            case rejected -> "REJECTED";
        };
    }
}
