package com.fpt.sealhackathon.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

public class ScoreSummaryResponse {

    private BigDecimal averageScore;

    private BigDecimal weightedScore;

    private BigDecimal variance;

    private BigDecimal standardDeviation;

}