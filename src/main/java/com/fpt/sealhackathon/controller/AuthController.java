package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.auth.AuthResponse;
import com.fpt.sealhackathon.dto.auth.ExternalRegisterRequest;
import com.fpt.sealhackathon.dto.auth.FptRegisterRequest;
import com.fpt.sealhackathon.dto.auth.LoginRequest;
import com.fpt.sealhackathon.dto.auth.MeResponse;
import com.fpt.sealhackathon.dto.auth.RefreshTokenRequest;
import com.fpt.sealhackathon.dto.auth.UserSummaryResponse;
import com.fpt.sealhackathon.dto.common.ApiResponse;
import com.fpt.sealhackathon.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Cac API dang ky, dang nhap va xac thuc JWT")
public class AuthController {

    private final AuthService authService;

    // Đăng ký tài khoản sinh viên FPT; sau khi validate request sẽ tạo user và role mặc định.
    @PostMapping("/register/fpt")
    @Operation(summary = "Dang ky tai khoan FPT", description = "Tao tai khoan cho sinh vien FPT voi campusId bat buoc")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Dang ky thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Du lieu khong hop le", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email da ton tai", content = @Content)
    })
    public ResponseEntity<ApiResponse<UserSummaryResponse>> registerFpt(
            @Valid @RequestBody FptRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Register FPT successfully", authService.registerFpt(request)));
    }

    // Đăng ký tài khoản external student với cùng flow nhưng không yêu cầu campusId.
    @PostMapping("/register/external")
    @Operation(summary = "Dang ky tai khoan External", description = "Tao tai khoan external student khong can campusId")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Dang ky thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Du lieu khong hop le", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email da ton tai", content = @Content)
    })
    public ResponseEntity<ApiResponse<UserSummaryResponse>> registerExternal(
            @Valid @RequestBody ExternalRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Register external successfully", authService.registerExternal(request)));
    }

    // Login nhận email/password và trả về access token, refresh token cùng thông tin user.
    @PostMapping("/login")
    @Operation(summary = "Dang nhap", description = "Xac thuc email va mat khau de nhan access token va refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dang nhap thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Du lieu khong hop le", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Thong tin dang nhap khong dung", content = @Content)
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successfully", authService.login(request)));
    }

    // Endpoint này dùng refresh token hợp lệ để xin cặp token mới mà không cần nhập lại mật khẩu.
    @PostMapping("/refresh-token")
    @Operation(summary = "Lam moi token", description = "Dua vao refresh token hop le de cap lai bo token moi")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lam moi token thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Du lieu khong hop le", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token khong hop le", content = @Content)
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Refresh token successfully",
                authService.refreshToken(request)
        ));
    }

    // Logout lấy access token từ header Authorization và đưa token đó vào blacklist.
    @PostMapping("/logout")
    @Operation(summary = "Dang xuat", description = "Vo hieu hoa access token hien tai bang blacklist trong bo nho")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dang xuat thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token khong hop le hoac da het han", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        authService.logout(authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success("Logout successfully", null));
    }

    // /auth/me không đọc body hay path param; nó dựa hoàn toàn vào Authentication hiện tại.
    @GetMapping("/me")
    @Operation(summary = "Lay thong tin current user", description = "Doc user hien tai tu Authentication da duoc JWT filter xac thuc")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay current user thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token khong hop le", content = @Content)
    })
    public ResponseEntity<ApiResponse<MeResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Get current user successfully",
                authService.getCurrentUser(authentication)
        ));
    }
}
