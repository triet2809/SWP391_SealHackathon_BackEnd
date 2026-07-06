package vn.edu.fpt.seal.modules.teamtimeline.mapper;

import vn.edu.fpt.seal.modules.teamtimeline.dto.TeamTimelineEventResponse;
import vn.edu.fpt.seal.modules.teamtimeline.entity.TeamTimelineEvent;

/** Mapper tĩnh entity -> DTO, theo cùng phong cách với các module khác. */
public final class TeamTimelineEventMapper {
    private TeamTimelineEventMapper() {}

    public static TeamTimelineEventResponse toResponse(TeamTimelineEvent e) {
        return TeamTimelineEventResponse.builder()
                .id(e.getId())
                .eventId(e.getEvent().getId())
                .teamId(e.getTeam().getId())
                .teamName(e.getTeam().getName())
                .roundId(e.getRound() == null ? null : e.getRound().getId())
                .roundName(e.getRound() == null ? null : e.getRound().getName())
                .type(e.getType())
                .title(e.getTitle())
                .description(e.getDescription())
                .scoreSnapshot(e.getScoreSnapshot())
                .rankSnapshot(e.getRankSnapshot())
                .statusSnapshot(e.getStatusSnapshot())
                .occurredAt(e.getOccurredAt())
                .build();
    }
}
