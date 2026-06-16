package com.fpt.sealhackathon.dto.event.response;

import java.util.UUID;

import com.fpt.sealhackathon.enums.TeamMemberRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberResponse {
    private UUID id;

    private Integer userId;

    private TeamMemberRole role;
}
