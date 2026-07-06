package com.fpt.sealhackathon.dto.campus;

import java.time.LocalDateTime;
import java.util.UUID;

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
public class CampusRequest {
    private UUID universityId;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Address must not be blank")
    private String address;

    @NotBlank(message = "City must not be blank")
    private String city;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
