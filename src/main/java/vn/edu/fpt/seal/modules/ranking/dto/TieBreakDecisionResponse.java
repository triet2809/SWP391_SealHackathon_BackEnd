package vn.edu.fpt.seal.modules.ranking.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTO trả về một quyết định phân định hòa thủ công. */
@Builder
public record TieBreakDecisionResponse(
        UUID id,
        UUID roundId,
        UUID teamId,
        String teamName,
        UUID decidedById,
        String decidedByName,
        LocalDateTime decidedAt,
        String reason,
        String evidenceUrl,
        String note
) {}
