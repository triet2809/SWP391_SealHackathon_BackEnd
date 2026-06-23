package com.fpt.sealhackathon.dto.track;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TrackResponse {

    private UUID id;
    private UUID eventId;
    private UUID roundId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
