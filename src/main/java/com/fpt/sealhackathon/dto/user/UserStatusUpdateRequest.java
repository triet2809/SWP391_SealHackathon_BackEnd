package com.fpt.sealhackathon.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request doi trang thai user")
public class UserStatusUpdateRequest {

    @Schema(
            description = "Trang thai moi. Chap nhan ACTIVE, PENDING, REJECTED hoac approved, pending, rejected",
            example = "ACTIVE"
    )
    @NotBlank(message = "Status is required")
    private String status;
}
