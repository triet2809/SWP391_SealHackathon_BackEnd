package vn.edu.fpt.seal.modules.team.dto;

import lombok.Builder;
import vn.edu.fpt.seal.common.enums.TeamStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record TeamResponse(
        UUID id,
        UUID trackId,
        UUID eventId,
        String name,
        String inviteCode,
        TeamStatus status,
        String disqualifiedReason,
        List<TeamMemberResponse> members,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
