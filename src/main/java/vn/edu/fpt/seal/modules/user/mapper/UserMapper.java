package vn.edu.fpt.seal.modules.user.mapper;

import vn.edu.fpt.seal.modules.user.dto.UserResponse;
import vn.edu.fpt.seal.modules.user.entity.*;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        var campus = u.getCampus();
        var university = campus != null ? campus.getUniversity() : u.getUniversity();
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .studentType(u.getStudentType())
                .studentId(u.getStudentId())
                .universityId(university == null ? null : university.getId())
                .universityName(university == null ? null : university.getName())
                .campusId(campus == null ? null : campus.getId())
                .campusName(campus == null ? null : campus.getName())
                .isGuest(u.isGuest())
                .status(u.getStatus())
                .roles(u.getRoles().stream().map(Role::getName).sorted().toList())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
