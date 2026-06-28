package com.fpt.sealhackathon.dto.criteriaTemplate;

import java.math.BigDecimal;

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
public class CriteriaTemplateRequest {

    @NotBlank(message = "Name must not be blank")
    private String name;

    private String description;

    @NotNull(message = "Default weight must not be null")
    @DecimalMin(value = "0.0", message = "Weight must be >= 0")
    private BigDecimal defaultWeight;
}
