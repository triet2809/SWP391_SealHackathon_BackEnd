package com.fpt.sealhackathon.dto.RoundCriteria;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fpt.sealhackathon.dto.RoundTrack.RoundTrackResponse;
import com.fpt.sealhackathon.dto.criteriaTemplate.CriteriaTemplateResponse;
import com.fpt.sealhackathon.dto.event.EventResponse;
import com.fpt.sealhackathon.dto.round.RoundResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoundCriteriaResponse {

    private UUID id;

    private EventResponse event;

    private RoundResponse round;

    private RoundTrackResponse roundTrack;

    private CriteriaTemplateResponse template;

    private String name;

    private BigDecimal weight;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
