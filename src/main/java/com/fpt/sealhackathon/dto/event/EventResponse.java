package com.fpt.sealhackathon.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fpt.sealhackathon.entity.enums.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private UUID id;
    private String title;
    private String seasonName;
    private Integer seasonYear;
    private String description;

    private EventStatus status;

    private LocalDateTime registrationStartAt;
    private LocalDateTime registrationEndAt;
    private LocalDateTime registrationClosedAt;

    private Integer minTeamSize;
    private Integer maxTeamSize;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
