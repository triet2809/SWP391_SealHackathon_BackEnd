package com.fpt.sealhackathon.dto.event;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EventResponse {

    private UUID id;
    private String title;
    private String seasonName;
    private Integer seasonYear;
    private String description;
    private String status;
    private LocalDateTime registrationStartAt;
    private LocalDateTime registrationEndAt;
    private LocalDateTime registrationClosedAt;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
