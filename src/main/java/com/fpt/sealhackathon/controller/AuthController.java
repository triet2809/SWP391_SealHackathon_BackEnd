package com.fpt.sealhackathon.controller;

import com.fpt.sealhackathon.dto.auth.AuthResponse;
import com.fpt.sealhackathon.dto.auth.ExternalRegisterRequest;
import com.fpt.sealhackathon.dto.auth.FptRegisterRequest;
import com.fpt.sealhackathon.dto.auth.GuestJudgeRegisterRequest;
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
// Controller gom cac API xac thuc va dang ky tai khoan cho nhieu doi tuong nguoi dung.
public class AuthController {

    private final AuthService authService;

    // Dang ky tai khoan sinh vien FPT; sau khi validate request se tao user va role mac dinh.
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

    // Dang ky tai khoan external student voi cung flow nhung khong yeu cau campusId.
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

    // Coordinator tao guest judge truc tiep va nhan ve thong tin user da duoc gan role judge.
    @PostMapping("/guest-judges")
    @Operation(summary = "Tao tai khoan guest judge", description = "Coordinator tao guest judge va tu dong gan role judge")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tao guest judge thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Du lieu khong hop le", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Khong co quyen coordinator", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email da ton tai", content = @Content)
    })
    public ResponseEntity<ApiResponse<UserSummaryResponse>> createGuestJudge(
            @Valid @RequestBody GuestJudgeRegisterRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Create guest judge successfully",
                        authService.createGuestJudge(request, authentication)
                ));
    }

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

    // Endpoint nay dung refresh token hop le de xin cap token moi ma khong can nhap lai mat khau.
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

    // Logout lay access token tu header Authorization va dua token do vao blacklist.
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

    // /auth/me khong doc body hay path param; no dua hoan toan vao Authentication hien tai.
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
