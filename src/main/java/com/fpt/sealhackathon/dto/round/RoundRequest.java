package com.fpt.sealhackathon.dto.round;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class RoundRequest {

    @NotNull(message = "Event id is required")
    private UUID eventId;

    @NotBlank(message = "Round name is required")
    @Size(max = 255, message = "Round name must not exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Sequence number is required")
    @Min(value = 1, message = "Sequence number must be greater than 0")
    private Integer sequenceNumber;

    private String status;

    private LocalDateTime startAt;

    private LocalDateTime submissionDeadline;

    private LocalDateTime scoringDeadline;

    private UUID createdBy;
}
