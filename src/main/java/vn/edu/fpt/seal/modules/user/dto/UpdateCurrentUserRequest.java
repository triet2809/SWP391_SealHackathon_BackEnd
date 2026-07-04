package vn.edu.fpt.seal.modules.user.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateCurrentUserRequest(
        @Size(max = 255) String fullName,
        @Size(max = 100) String studentId,
        @Size(max = 50) String phone,
        @Size(max = 255) String department,
        @Size(max = 255) String position,
        UUID universityId,
        UUID campusId
) {}
