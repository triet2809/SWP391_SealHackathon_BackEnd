package com.fpt.sealhackathon.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockTeamRequest {

    // Optional note shown as the team's locked reason.
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}
