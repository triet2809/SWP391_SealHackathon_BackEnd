package vn.edu.fpt.seal.modules.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateTeamRequest(
        @NotNull UUID trackId,
        @NotBlank @Size(max = 255) String name,
        UUID leaderUserId,
        List<UUID> memberUserIds
) {
}
