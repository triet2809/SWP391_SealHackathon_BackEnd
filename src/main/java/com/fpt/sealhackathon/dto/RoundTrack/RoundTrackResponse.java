package com.fpt.sealhackathon.dto.RoundTrack;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fpt.sealhackathon.dto.event.EventResponse;
import com.fpt.sealhackathon.dto.round.RoundResponse;

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
public class RoundTrackResponse {

    private UUID id;

    private EventResponse event;

    private RoundResponse round;

    private String name;

    private String challengeTitle;

    private String challengeDescription;

    private String challengeFileUrl;

    private Integer maxTeams;

    private Integer topNToPromote;

    private Integer displayOrder;

    private Boolean isFinalSharedTrack;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
