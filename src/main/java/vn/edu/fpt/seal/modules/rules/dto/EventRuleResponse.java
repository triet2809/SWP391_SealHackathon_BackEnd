package vn.edu.fpt.seal.modules.rules.dto;

import lombok.Builder;
import vn.edu.fpt.seal.common.enums.RuleVisibility;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTO trả về một điều luật của sự kiện. */
@Builder
public record EventRuleResponse(
        UUID id,
        UUID eventId,
        String title,
        String content,
        RuleVisibility visibility,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
