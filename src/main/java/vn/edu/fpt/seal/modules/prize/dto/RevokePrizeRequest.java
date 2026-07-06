package vn.edu.fpt.seal.modules.prize.dto;

import jakarta.validation.constraints.NotBlank;

/** Yêu cầu thu hồi giải thưởng khỏi đội hiện tại (bắt buộc nêu lý do). */
public record RevokePrizeRequest(
        @NotBlank String reason,
        String evidenceNote
) {}
