package vn.edu.fpt.seal.modules.prize.dto;

import lombok.Builder;
import vn.edu.fpt.seal.common.enums.PrizeRevisionAction;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTO trả về một bản ghi lịch sử chỉnh sửa giải thưởng. */
@Builder
public record PrizeRevisionResponse(
        UUID id,
        UUID prizeId,
        PrizeRevisionAction action,
        UUID oldTeamId,
        String oldTeamName,
        UUID newTeamId,
        String newTeamName,
        String reason,
        String evidenceNote,
        UUID changedById,
        String changedByName,
        LocalDateTime changedAt
) {}
