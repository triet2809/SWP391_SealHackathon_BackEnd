package com.fpt.sealhackathon.dto.round;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fpt.sealhackathon.entity.enums.RoundStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoundRequest {

    private UUID eventId;

    private String name;

    private String description;

    private Integer sequenceNumber;

    private RoundStatus status;

    private LocalDateTime startAt;

    private LocalDateTime submissionDeadline;

    private LocalDateTime scoringDeadline;
}
