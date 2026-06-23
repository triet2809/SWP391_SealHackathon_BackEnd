package com.fpt.sealhackathon.dto.round;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RoundResponse {

    private UUID id;
    private UUID eventId;
    private String name;
    private Integer sequenceNumber;
    private LocalDateTime submissionDeadline;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
