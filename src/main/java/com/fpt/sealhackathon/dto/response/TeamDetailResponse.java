package com.fpt.sealhackathon.dto.response;

import com.fpt.sealhackathon.dto.enums.RegistrationMode;
import com.fpt.sealhackathon.dto.enums.TeamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDetailResponse {
    private UUID id;

    private UUID eventId;

    private UUID teamProfileId;

    private String name;

    private String description;

    private TeamStatus status;

    private RegistrationMode registrationMode;

    private boolean isLocked;

    private String lockedReason;

    private int minMemberCount;

    private int maxMemberCount;

    private Instant approvedAt;

    private String disqualifiedReason;

    private String eliminatedReason;

    private UUID createdBy;

    private Instant createdAt;

    private Instant updatedAt;

    private List<TeamMemberResponse> members;
}