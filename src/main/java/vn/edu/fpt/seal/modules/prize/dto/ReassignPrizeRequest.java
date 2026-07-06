package vn.edu.fpt.seal.modules.prize.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Yêu cầu chuyển giải thưởng sang đội khác (bắt buộc nêu lý do). */
public record ReassignPrizeRequest(
        @NotNull UUID newTeamId,
        @NotBlank String reason,
        String evidenceNote
) {}
