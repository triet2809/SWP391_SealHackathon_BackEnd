package com.fpt.sealhackathon.dto.auth;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UserSummaryResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String campusId;
    private String studentType;
    private String status;
    private List<String> roles;
}
