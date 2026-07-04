package vn.edu.fpt.seal.modules.notification.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String body,
        String category,
        String refType,
        UUID refId,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
