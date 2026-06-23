package com.fpt.sealhackathon.dto.roundtrack;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RoundTrackRequest {

    @NotNull(message = "Event id is required")
    private UUID eventId;

    @NotNull(message = "Round id is required")
    private UUID roundId;

    @NotBlank(message = "Round track name is required")
    @Size(max = 255, message = "Round track name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Challenge title must not exceed 255 characters")
    private String challengeTitle;

    private String challengeDescription;

    @Size(max = 500, message = "Challenge file URL must not exceed 500 characters")
    private String challengeFileUrl;

    @Min(value = 1, message = "Max teams must be greater than 0")
    private Integer maxTeams;

    @Min(value = 0, message = "Top N to promote must be greater than or equal to 0")
    private Integer topNToPromote;

    @Min(value = 1, message = "Display order must be greater than 0")
    private Integer displayOrder;

    private Boolean isFinalSharedTrack;

    private UUID createdBy;
}
