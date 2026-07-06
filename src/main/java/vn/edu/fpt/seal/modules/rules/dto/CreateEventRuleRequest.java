package vn.edu.fpt.seal.modules.rules.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.edu.fpt.seal.common.enums.RuleVisibility;

import java.util.UUID;

/** Yêu cầu tạo mới một điều luật cho sự kiện (chỉ điều phối viên). */
public record CreateEventRuleRequest(
        @NotNull UUID eventId,
        @NotBlank @Size(max = 255) String title,
        @NotBlank String content,
        RuleVisibility visibility,
        Integer displayOrder
) {}
