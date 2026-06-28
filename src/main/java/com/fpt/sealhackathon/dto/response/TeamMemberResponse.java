package com.fpt.sealhackathon.dto.response;

import com.fpt.sealhackathon.dto.enums.TeamMemberRole;
import com.fpt.sealhackathon.dto.enums.TeamMemberStatus;
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
public class TeamMemberResponse {

    private UUID id;

    private UUID teamId;

    private UUID userId;

    private String fullName;

    private String email;

    private String studentType;

    private TeamMemberRole role;

    private TeamMemberStatus status;

    private Instant joinedAt;

    private Instant acceptedAt;

    private Instant declinedAt;

    private Instant removedAt;
}