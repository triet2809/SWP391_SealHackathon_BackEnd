package com.fpt.sealhackathon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisqualifyTeamRequest {

    // Required justification, stored as the team's disqualified reason.
    @NotBlank(message = "Reason must not be blank")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}
