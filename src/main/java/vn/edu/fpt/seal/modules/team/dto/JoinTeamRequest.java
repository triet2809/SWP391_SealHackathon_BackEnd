package vn.edu.fpt.seal.modules.team.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinTeamRequest(
        @NotBlank String inviteCode,
        // Checkbox "đồng ý với thể lệ sự kiện": bắt buộc = true nếu sự kiện có rule PUBLIC
        // và người tham gia chưa từng chấp nhận trước đó.
        Boolean acceptedRules
) {}
