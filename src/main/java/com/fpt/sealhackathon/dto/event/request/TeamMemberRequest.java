package com.fpt.sealhackathon.dto.event.request;


import com.fpt.sealhackathon.enums.TeamMemberRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeamMemberRequest {

    //
    private Integer userId;

    private TeamMemberRole role = TeamMemberRole.MEMBER;
}
