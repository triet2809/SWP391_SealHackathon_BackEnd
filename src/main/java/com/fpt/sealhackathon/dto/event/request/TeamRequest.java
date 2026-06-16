package com.fpt.sealhackathon.dto.event.request;

import java.util.Set;
import java.util.UUID;

import com.fpt.sealhackathon.enums.TeamStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeamRequest {

    @NotNull(message = "Track id is required")
    private UUID trackId;

    @NotBlank(message = "Team name is required")
    private String name;

    private TeamStatus status = TeamStatus.ACTIVE;

    private String disqualifiedReason;

    @Size(
        min = 3,
        max = 5,
        message = "Team must have between 3 and 5 members"
    )
    @Valid
    private Set<TeamMemberRequest> members;
}
