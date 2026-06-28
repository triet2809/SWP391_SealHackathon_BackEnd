package com.fpt.sealhackathon.dto.event;

import com.fpt.sealhackathon.entity.enums.EventStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventStatusRequest {

    @NotNull(message = "Status must not be null")
    private EventStatus status;
}
