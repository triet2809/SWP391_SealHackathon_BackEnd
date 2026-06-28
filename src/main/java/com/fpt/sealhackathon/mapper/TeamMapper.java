package com.fpt.sealhackathon.mapper;

import com.fpt.sealhackathon.dto.response.TeamDetailResponse;
import com.fpt.sealhackathon.dto.response.TeamMemberResponse;
import com.fpt.sealhackathon.dto.response.TeamSummaryResponse;
import com.fpt.sealhackathon.entity.Team;
import com.fpt.sealhackathon.entity.TeamMember;
import org.springframework.stereotype.Component;

import java.util.List;

/** Converts Team/TeamMember entities to response DTOs. No business logic. */
@Component
public class TeamMapper {

    /** Maps a {@link TeamMember} entity to {@link TeamMemberResponse}. */
    public TeamMemberResponse toMemberResponse(TeamMember member) {
        return TeamMemberResponse.builder()
                .id(member.getId())
                .teamId(member.getTeam().getId())
                .userId(member.getUser().getId())
                .fullName(null)   // populated from User entity when BE2 expands User stub
                .email(null)      // populated from User entity when BE2 expands User stub
                .studentType(null)// populated from User entity when BE2 expands User stub
                .role(member.getRole())
                .status(member.getStatus())
                .joinedAt(member.getJoinedAt())
                .acceptedAt(member.getAcceptedAt())
                .declinedAt(member.getDeclinedAt())
                .removedAt(member.getRemovedAt())
                .build();
    }

    /** Maps a list of {@link TeamMember} entities to a list of {@link TeamMemberResponse}. */
    public List<TeamMemberResponse> toMemberResponseList(List<TeamMember> members) {
        return members.stream()
                .map(this::toMemberResponse)
                .toList();
    }

    /** Maps a {@link Team} entity to {@link TeamDetailResponse} including all members. */
    public TeamDetailResponse toDetailResponse(Team team) {
        return TeamDetailResponse.builder()
                .id(team.getId())
                .eventId(team.getEvent().getId())
                .teamProfileId(team.getTeamProfile().getId())
                .name(team.getName())
                .description(team.getTeamProfile().getDescription())
                .status(team.getStatus())
                .registrationMode(team.getRegistrationMode())
                .isLocked(team.isLocked())
                .lockedReason(team.getLockedReason())
                .minMemberCount(team.getEvent().getMinTeamSize())
                .maxMemberCount(team.getEvent().getMaxTeamSize())
                .approvedAt(team.getApprovedAt())
                .disqualifiedReason(team.getDisqualifiedReason())
                .eliminatedReason(team.getEliminatedReason())
                .createdBy(team.getCreatedBy() != null ? team.getCreatedBy().getId() : null)
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .members(toMemberResponseList(team.getMembers()))
                .build();
    }

    /** Maps a {@link Team} entity to lightweight {@link TeamSummaryResponse} (no members). */
    public TeamSummaryResponse toSummaryResponse(Team team, int memberCount) {
        return TeamSummaryResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .status(team.getStatus())
                .registrationMode(team.getRegistrationMode())
                .memberCount(memberCount)
                .minMemberCount(team.getEvent().getMinTeamSize())
                .maxMemberCount(team.getEvent().getMaxTeamSize())
                .isLocked(team.isLocked())
                .approvedAt(team.getApprovedAt())
                .createdAt(team.getCreatedAt())
                .build();
    }
}