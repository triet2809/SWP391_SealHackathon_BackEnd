package vn.edu.fpt.seal.modules.user.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateCurrentUserRequest(
        @Size(max = 255) String fullName,
        @Size(max = 100) String studentId,
        UUID universityId,
        UUID campusId
) {}
