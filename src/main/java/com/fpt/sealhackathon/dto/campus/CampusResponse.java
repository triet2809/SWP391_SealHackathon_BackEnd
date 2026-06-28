package com.fpt.sealhackathon.dto.campus;

import java.time.LocalDateTime;
import java.util.UUID;


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
public class CampusResponse {
    private UUID id;

    private String name;

    private String address;

    private String city;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
