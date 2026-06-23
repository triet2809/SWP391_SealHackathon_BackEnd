package com.fpt.sealhackathon.dto.criteriatemplate;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CriteriaTemplateResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal defaultWeight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
