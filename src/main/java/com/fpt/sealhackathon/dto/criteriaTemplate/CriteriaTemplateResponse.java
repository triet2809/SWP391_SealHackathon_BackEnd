package com.fpt.sealhackathon.dto.criteriaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaTemplateResponse {

    private UUID id;

    private String name;

    private String description;

    private BigDecimal defaultWeight;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
