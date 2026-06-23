package com.fpt.sealhackathon.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EventRequest {

    @NotBlank(message = "Event title is required")
    @Size(max = 255, message = "Event title must not exceed 255 characters")
    private String title;

    @Size(max = 50, message = "Season name must not exceed 50 characters")
    private String seasonName;

    @Min(value = 2000, message = "Season year must be at least 2000")
    private Integer seasonYear;

    private String description;

    private String status;

    private LocalDateTime registrationStartAt;

    private LocalDateTime registrationEndAt;

    private LocalDateTime registrationClosedAt;

    @Min(value = 1, message = "Min team size must be greater than 0")
    private Integer minTeamSize;

    @Min(value = 1, message = "Max team size must be greater than 0")
    private Integer maxTeamSize;

    private UUID createdBy;
}
