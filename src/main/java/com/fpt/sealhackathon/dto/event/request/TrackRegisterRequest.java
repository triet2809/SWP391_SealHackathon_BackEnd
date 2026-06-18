package com.fpt.sealhackathon.dto.event.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackRegisterRequest {

    @NotNull(message = "Envent id is required")
    private UUID eventId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
