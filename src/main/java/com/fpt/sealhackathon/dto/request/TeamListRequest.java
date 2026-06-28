package com.fpt.sealhackathon.dto.request;

import com.fpt.sealhackathon.dto.enums.TeamStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamListRequest {

    private TeamStatus status;

    private String search;

    @Builder.Default
    @Min(value = 0, message = "Page must be >= 0")
    private int page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be >= 1")
    @Max(value = 100, message = "Size must be <= 100")
    private int size = 20;
}