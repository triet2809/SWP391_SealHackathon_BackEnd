package com.fpt.sealhackathon.dto.university;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
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
public class UniversityRequest {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Short name must not be blank")
    private String shortName;

    @NotBlank(message = "Country must not be blank")
    private String country;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
