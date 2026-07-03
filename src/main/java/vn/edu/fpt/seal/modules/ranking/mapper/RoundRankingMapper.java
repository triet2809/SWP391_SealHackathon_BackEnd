package vn.edu.fpt.seal.modules.ranking.mapper;

import vn.edu.fpt.seal.modules.ranking.dto.RoundRankingResponse;
import vn.edu.fpt.seal.modules.ranking.entity.RoundRanking;

public final class RoundRankingMapper {
    private RoundRankingMapper() {}
    public static RoundRankingResponse toResponse(RoundRanking r) {
        return RoundRankingResponse.builder()
                .id(r.getId()).roundId(r.getRound().getId()).teamId(r.getTeam().getId()).teamName(r.getTeam().getName())
                .totalScore(r.getTotalScore()).rank(r.getRank()).status(r.getStatus())
                .tieBreakerCriterionId(r.getTieBreakerCriterion() == null ? null : r.getTieBreakerCriterion().getId())
                .tieBreakerScore(r.getTieBreakerScore()).tieBreakerReason(r.getTieBreakerReason())
                .calculatedAt(r.getCalculatedAt()).updatedAt(r.getUpdatedAt()).build();
    }
}
