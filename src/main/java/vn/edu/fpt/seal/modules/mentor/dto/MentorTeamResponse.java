package vn.edu.fpt.seal.modules.mentor.dto;

import lombok.Builder;
import vn.edu.fpt.seal.common.enums.TeamStatus;
import java.util.UUID;

@Builder
public record MentorTeamResponse(UUID trackMentorId, UUID mentorId, UUID trackId, String trackName, UUID roundId, String roundName, UUID teamId, String teamName, TeamStatus teamStatus) {}
