package com.fpt.sealhackathon.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
public class ScoreRequest {
    private UUID criterionId;

    private BigDecimal score;

    private String comment;
    private UUID judgeId;
}
