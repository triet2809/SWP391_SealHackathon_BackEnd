package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.user.UserManagementResponse;
import com.fpt.sealhackathon.dto.user.UserStatusUpdateRequest;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface UserManagementService {

    List<UserManagementResponse> getUsers(String role, String status, String search);

    UserManagementResponse getUserById(UUID userId);

    List<UserManagementResponse> getPendingUsers();

    UserManagementResponse approveUser(UUID userId, Authentication authentication);

    UserManagementResponse rejectUser(UUID userId, Authentication authentication);

    UserManagementResponse updateStatus(UUID userId, UserStatusUpdateRequest request, Authentication authentication);
}
