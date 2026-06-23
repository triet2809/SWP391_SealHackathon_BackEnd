package com.fpt.sealhackathon.dto.roundcriteria;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RoundCriteriaResponse {

    private UUID id;
    private UUID eventId;
    private UUID roundId;
    private UUID roundTrackId;
    private UUID templateId;
    private String name;
    private BigDecimal weight;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
