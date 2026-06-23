package com.fpt.sealhackathon.dto.roundcriteria;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class RoundCriteriaRequest {

    @NotNull(message = "Event id is required")
    private UUID eventId;

    @NotNull(message = "Round id is required")
    private UUID roundId;

    private UUID roundTrackId;

    private UUID templateId;

    @NotBlank(message = "Criteria name is required")
    @Size(max = 255, message = "Criteria name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Weight must be greater than or equal to 0")
    private BigDecimal weight;

    private String description;
}
