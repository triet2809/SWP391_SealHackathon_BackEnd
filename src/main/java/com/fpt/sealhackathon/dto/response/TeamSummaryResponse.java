package com.fpt.sealhackathon.dto.response;

import com.fpt.sealhackathon.dto.enums.RegistrationMode;
import com.fpt.sealhackathon.dto.enums.TeamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamSummaryResponse {
    private UUID id;

    private String name;

    private TeamStatus status;

    private RegistrationMode registrationMode;

    private int memberCount;

    private int maxMemberCount;

    private int minMemberCount;

    private boolean isLocked;

    private Instant approvedAt;

    private Instant createdAt;
}