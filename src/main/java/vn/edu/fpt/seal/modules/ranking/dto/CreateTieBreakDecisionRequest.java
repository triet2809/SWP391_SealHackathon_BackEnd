package vn.edu.fpt.seal.modules.ranking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Yêu cầu tạo quyết định phân định hòa thủ công (review mã nguồn GitHub). */
public record CreateTieBreakDecisionRequest(
        @NotNull UUID teamId,
        @NotBlank String reason,
        @Size(max = 500) String evidenceUrl,
        String note
) {}
