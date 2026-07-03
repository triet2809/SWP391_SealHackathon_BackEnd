package vn.edu.fpt.seal.modules.ranking.dto;

import lombok.Builder;
import vn.edu.fpt.seal.common.enums.PromotionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RoundRankingResponse(UUID id, UUID roundId, UUID teamId, String teamName, BigDecimal totalScore, Integer rank, PromotionStatus status, UUID tieBreakerCriterionId, BigDecimal tieBreakerScore, String tieBreakerReason, LocalDateTime calculatedAt, LocalDateTime updatedAt) {}
