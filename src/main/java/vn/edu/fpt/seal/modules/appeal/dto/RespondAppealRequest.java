package vn.edu.fpt.seal.modules.appeal.dto;

import jakarta.validation.constraints.NotBlank;

/** Điều phối viên ghi/ cập nhật phản hồi mà chưa chốt kết luận (đơn vẫn PENDING). */
public record RespondAppealRequest(@NotBlank String response) {}
