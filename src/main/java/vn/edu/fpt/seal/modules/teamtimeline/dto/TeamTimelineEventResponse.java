package vn.edu.fpt.seal.modules.teamtimeline.dto;

import lombok.Builder;
import vn.edu.fpt.seal.common.enums.TimelineEventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** DTO trả về một mốc trên timeline của đội. */
@Builder
public record TeamTimelineEventResponse(
        UUID id,
        UUID eventId,
        UUID teamId,
        String teamName,
        UUID roundId,
        String roundName,
        TimelineEventType type,
        String title,
        String description,
        BigDecimal scoreSnapshot,
        Integer rankSnapshot,
        String statusSnapshot,
        LocalDateTime occurredAt
) {}
