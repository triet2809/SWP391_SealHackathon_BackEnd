package vn.edu.fpt.seal.modules.appeal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Yêu cầu nộp khiếu nại: đội được suy ra từ tư cách thành viên của người gọi trong track của vòng. */
public record CreateAppealRequest(
        @NotNull UUID roundId,
        @NotBlank String reason
) {}
