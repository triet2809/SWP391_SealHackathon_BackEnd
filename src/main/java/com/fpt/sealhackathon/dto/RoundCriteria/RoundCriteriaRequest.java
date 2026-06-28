package com.fpt.sealhackathon.dto.RoundCriteria;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoundCriteriaRequest {

    @NotNull
    private UUID eventId;

    @NotNull
    private UUID roundId;

    private UUID roundTrackId;

    private UUID templateId;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal weight;

    private String description;
}
