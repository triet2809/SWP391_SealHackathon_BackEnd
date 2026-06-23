package com.fpt.sealhackathon.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "Thong tin role")
public class RoleResponse {

    @Schema(description = "ID cua role")
    private UUID id;

    @Schema(description = "Ten role dung trong he thong", example = "coordinator")
    private String name;

    @Schema(description = "Mo ta role")
    private String description;
}
