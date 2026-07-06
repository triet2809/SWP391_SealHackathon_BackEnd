package vn.edu.fpt.seal.modules.appeal.dto;

import lombok.Builder;
import vn.edu.fpt.seal.common.enums.AppealStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTO trả về đầy đủ thông tin một đơn khiếu nại. */
@Builder
public record AppealResponse(
        UUID id,
        UUID eventId,
        UUID roundId,
        String roundName,
        UUID teamId,
        String teamName,
        UUID submittedById,
        String submittedByName,
        String reason,
        AppealStatus status,
        String response,
        UUID resolvedById,
        String resolvedByName,
        LocalDateTime resultPublishedAt,
        LocalDateTime appealDeadline,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
