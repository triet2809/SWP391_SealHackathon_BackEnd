package com.fpt.sealhackathon.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import com.fpt.sealhackathon.dto.event.request.TeamMemberRequest;
import com.fpt.sealhackathon.dto.event.request.TeamRequest;
import com.fpt.sealhackathon.dto.event.response.TeamMemberResponse;
import com.fpt.sealhackathon.dto.event.response.TeamResponse;
import com.fpt.sealhackathon.entity.Team;
import com.fpt.sealhackathon.entity.TeamMember;
import com.fpt.sealhackathon.entity.Track;
import com.fpt.sealhackathon.entity.Users;
import com.fpt.sealhackathon.enums.TeamMemberRole;
import com.fpt.sealhackathon.enums.TeamStatus;
import com.fpt.sealhackathon.exception.ResourceNotFoundException;
import com.fpt.sealhackathon.repository.TeamMemberRepository;
import com.fpt.sealhackathon.repository.TeamRepository;
import com.fpt.sealhackathon.repository.TrackRepository;
import com.fpt.sealhackathon.repository.UserRepository;
import com.fpt.sealhackathon.service.TeamService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TeamServiceImpl implements TeamService {
    
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public TeamResponse createTeam(TeamRequest request) {

        Track track = trackRepository.findById(request.getTrackId())
        .orElseThrow(() -> new ResourceNotFoundException("Track not found"));

        Team team = Team.builder().track(track)
                    .name(request.getName())
                    .status(TeamStatus.ACTIVE)
                    .disqualifiedReason(request.getDisqualifiedReason()).build();
        Team teamSaved = teamRepository.save(team);

        Set<Integer> userIds = request.getMembers()
            .stream()
            .map(TeamMemberRequest::getUserId)
            .collect(Collectors.toSet());

        if (userIds.size() != request.getMembers().size()) {
            throw new ResourceNotFoundException("Duplicate users in team");
        }

        List<TeamMember> members = new ArrayList<>();

        for (TeamMemberRequest memberRequest : request.getMembers()) {

            Users user = userRepository.findById(memberRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            TeamMember member = TeamMember.builder()
                .team(teamSaved)
                .user(user)
                .role(TeamMemberRole.MEMBER)
                .build();

            members.add(member);
        }

        return TeamResponse.builder()
            .id(teamSaved.getId())
            .trackId(teamSaved.getTrack().getId())
            .name(teamSaved.getName())
            .status(teamSaved.getStatus())
            .disqualifiedReason(teamSaved.getDisqualifiedReason())
            .members(
                teamMemberRepository.saveAll(members)
                        .stream()
                        .map(member -> TeamMemberResponse.builder()
                                .id(member.getId())
                                .userId(member.getUser().getId())
                                .role(member.getRole())
                                .build())
                        .collect(Collectors.toSet())
        )
        .build();

    }
    
}
