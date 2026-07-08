package com.fpt.sealhackathon.dto.round;

import java.time.LocalDateTime;

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
public class RoundRequest {

    private String name;

    private String description;

    private Integer sequenceNumber;

    private LocalDateTime startAt;

    private LocalDateTime submissionDeadline;

    private LocalDateTime scoringDeadline;
}
