package com.fpt.sealhackathon.dto.university;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fpt.sealhackathon.dto.campus.CampusResponse;

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
public class UniversityResponse {
    private UUID id;

    private String name;

    private String shortName;

    private String country;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    private List<CampusResponse> campuses;
}
