package com.fpt.sealhackathon.dto.round;

import jakarta.validation.constraints.Future;
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

    @NotNull(message = "Sequence number is required")
    @Min(value = 1, message = "Sequence number must be greater than 0")
    private Integer sequenceNumber;

    @NotNull(message = "Submission deadline is required")
    @Future(message = "Submission deadline must be in the future")
    private LocalDateTime submissionDeadline;
}
