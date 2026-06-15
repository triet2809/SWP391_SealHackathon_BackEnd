package com.fpt.sealhackathon.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequest {

    @NotBlank(message = "Event title is required")
    @Size(max = 255, message = "Event title must not exceed 255 characters")
    private String title;

    private String description;

    private String status;
}