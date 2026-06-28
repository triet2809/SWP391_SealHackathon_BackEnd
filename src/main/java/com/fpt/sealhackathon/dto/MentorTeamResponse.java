package com.fpt.sealhackathon.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MentorTeamResponse {

    private UUID teamId;

    private String teamName;

    private UUID roundTrackId;
}