package vn.edu.fpt.seal.modules.team.mapper;

import vn.edu.fpt.seal.modules.team.dto.TeamMemberResponse;
import vn.edu.fpt.seal.modules.team.dto.TeamResponse;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.team.entity.TeamMember;

import java.util.List;

public final class TeamMapper {

    private TeamMapper() {
    }

    public static TeamResponse toResponse(Team team, List<TeamMember> members) {
        return TeamResponse.builder()
                .id(team.getId())
                .trackId(team.getTrack().getId())
                .eventId(team.getTrack().getEvent().getId())
                .name(team.getName())
                .status(team.getStatus())
                .disqualifiedReason(team.getDisqualifiedReason())
                .members(members.stream().map(TeamMapper::toMemberResponse).toList())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    public static TeamMemberResponse toMemberResponse(TeamMember member) {
        return TeamMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .email(member.getUser().getEmail())
                .fullName(member.getUser().getFullName())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
