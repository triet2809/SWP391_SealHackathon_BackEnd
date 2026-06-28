package com.fpt.sealhackathon.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class JudgeAssignRequest {
    private UUID userId;
    private UUID roundTrackId;
}
