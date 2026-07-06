package vn.edu.fpt.seal.modules.rules.dto;

import jakarta.validation.constraints.Size;
import vn.edu.fpt.seal.common.enums.RuleVisibility;

/** Yêu cầu cập nhật điều luật — mọi field đều optional (partial update). */
public record UpdateEventRuleRequest(
        @Size(max = 255) String title,
        String content,
        RuleVisibility visibility,
        Integer displayOrder
) {}
