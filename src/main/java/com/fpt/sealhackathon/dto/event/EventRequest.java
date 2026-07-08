package com.fpt.sealhackathon.dto.event;

import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title max 255 characters")
    private String title;

    @Size(max = 50, message = "Season name max 50 characters")
    private String seasonName;

    @Min(value = 2000, message = "Season year must be >= 2000")
    private Integer seasonYear;

    private String description;

    private LocalDateTime registrationStartAt;

    private LocalDateTime registrationEndAt;

    private LocalDateTime registrationClosedAt;

    @Default
    @Min(value = 1, message = "Min team size must be > 0")
    private Integer minTeamSize = 3;

    @Default
    @Min(value = 1, message = "Max team size must be > 0")
    private Integer maxTeamSize = 5;

    @AssertTrue(message = "Max team size must be >= min team size")
    public boolean isTeamSizeValid() {
        if (minTeamSize == null || maxTeamSize == null)
            return true;
        return maxTeamSize >= minTeamSize;
    }

    @AssertTrue(message = "Registration start must be before end")
    public boolean isRegistrationWindowValid() {
        if (registrationStartAt == null || registrationEndAt == null)
            return true;
        return registrationStartAt.isBefore(registrationEndAt);
    }

    @AssertTrue(message = "Registration closed must be after start/end")
    public boolean isClosedDateValid() {
        if (registrationClosedAt == null)
            return true;
        if (registrationStartAt != null && registrationClosedAt.isBefore(registrationStartAt))
            return false;
        if (registrationEndAt != null && registrationClosedAt.isBefore(registrationEndAt))
            return false;
        return true;
    }
}
