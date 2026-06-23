package com.fpt.sealhackathon.dto.track;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TrackRequest {

    @NotNull(message = "Event id is required")
    private UUID eventId;

    @NotNull(message = "Round id is required")
    private UUID roundId;

    @NotBlank(message = "Track name is required")
    @Size(max = 255, message = "Track name must not exceed 255 characters")
    private String name;

    private String description;
}
