package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.role.RoleResponse;
import com.fpt.sealhackathon.dto.user.UserManagementResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    List<RoleResponse> getRoles();

    UserManagementResponse assignRole(UUID roleId, UUID userId);

    UserManagementResponse removeRole(UUID roleId, UUID userId);
}
