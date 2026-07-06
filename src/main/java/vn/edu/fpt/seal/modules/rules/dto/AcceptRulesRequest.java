package vn.edu.fpt.seal.modules.rules.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Yêu cầu chấp nhận thể lệ của một sự kiện.
 * accepted phải là true — tương ứng checkbox "I agree to the event rules" trên UI.
 */
public record AcceptRulesRequest(
        @NotNull UUID eventId,
        @AssertTrue(message = "You must accept the event rules") boolean accepted
) {}
