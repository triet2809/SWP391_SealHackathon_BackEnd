package vn.edu.fpt.seal.modules.rules.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTO trả về trạng thái chấp nhận thể lệ của người dùng với một sự kiện. */
@Builder
public record RuleAcceptanceResponse(
        UUID id,
        UUID userId,
        UUID eventId,
        boolean accepted,
        LocalDateTime acceptedAt
) {}
