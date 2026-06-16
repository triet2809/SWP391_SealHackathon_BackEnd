package com.fpt.sealhackathon.dto.event.response;

import java.util.Set;
import java.util.UUID;

import com.fpt.sealhackathon.enums.TeamStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamResponse {

    private UUID id;

    private UUID trackId;

    private String name;

    private TeamStatus status;

    private String disqualifiedReason;

    private Set<TeamMemberResponse> members;
}
