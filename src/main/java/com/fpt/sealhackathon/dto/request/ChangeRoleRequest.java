package com.fpt.sealhackathon.dto.request;

import com.fpt.sealhackathon.dto.enums.TeamMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {

    @NotNull(message = "role must not be null")
    private TeamMemberRole role;
}