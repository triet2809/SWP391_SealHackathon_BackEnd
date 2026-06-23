package com.fpt.sealhackathon.dto.criteriatemplate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CriteriaTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Template name must not exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Default weight is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Default weight must be greater than or equal to 0")
    private BigDecimal defaultWeight;
}
