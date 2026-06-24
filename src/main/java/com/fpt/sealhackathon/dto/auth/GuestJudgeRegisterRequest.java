package com.fpt.sealhackathon.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// DTO nhan du lieu tao tai khoan guest judge do coordinator cung cap.
public class GuestJudgeRegisterRequest {

    @Schema(description = "Ho va ten day du cua guest judge", example = "Nguyen Thi Judge")
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Schema(description = "Email dung de dang nhap", example = "judge.guest@gmail.com")
    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Mat khau toi thieu 6 ky tu", example = "123456")
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
