package com.fpt.sealhackathon.dto.roundtrack;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RoundTrackResponse {

    private UUID id;
    private UUID eventId;
    private UUID roundId;
    private String name;
    private String challengeTitle;
    private String challengeDescription;
    private String challengeFileUrl;
    private Integer maxTeams;
    private Integer topNToPromote;
    private Integer displayOrder;
    private Boolean isFinalSharedTrack;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
