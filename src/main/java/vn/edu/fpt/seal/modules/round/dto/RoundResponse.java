package vn.edu.fpt.seal.modules.round.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RoundResponse(
        UUID id,
        UUID trackId,
        UUID eventId,
        String name,
        Integer sequenceNumber,
        LocalDateTime submissionDeadline,
        Integer topNToPromote,
        // Thông tin cửa sổ khiếu nại — FE dùng để hiển thị nút khiếu nại + đồng hồ đếm ngược
        LocalDateTime resultPublishedAt,
        LocalDateTime appealDeadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
