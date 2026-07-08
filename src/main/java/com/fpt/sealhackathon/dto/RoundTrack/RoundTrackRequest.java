package com.fpt.sealhackathon.dto.RoundTrack;

import java.util.UUID;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Comments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class RoundTrackRequest {

    @NotNull(message = "Event id must not be null")
    private UUID eventId;

    @Schema(description = "Ignored in create request. The round ID is obtained from the path variable.")
    @NotNull(message = "Round id must not be null")
    private UUID roundId;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Challenge title must not exceed 255 characters")
    private String challengeTitle;

    private String challengeDescription;

    @Size(max = 500, message = "Challenge file URL must not exceed 500 characters")
    private String challengeFileUrl;

    @Positive(message = "Max teams must be greater than 0")
    private Integer maxTeams;

    @NotNull(message = "Top N to promote must not be null")
    @Min(value = 0, message = "Top N to promote must be greater than or equal to 0")
    private Integer topNToPromote;

    @NotNull(message = "Display order must not be null")
    @Min(value = 1, message = "Display order must be greater than 0")
    private Integer displayOrder;

    @NotNull(message = "Final shared track flag must not be null")
    private Boolean isFinalSharedTrack;
}
