package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.auth.application.dto.request.LoginRequest;
import com.axiserp.auth.application.dto.request.PasswordResetRequest;
import com.axiserp.auth.application.dto.request.ResetPasswordRequest;
import com.axiserp.auth.application.dto.response.LoginResponse;
import com.axiserp.auth.application.dto.response.UserInfoResponse;
import com.axiserp.auth.infrastructure.adapters.in.web.response.ApiResponse;
import com.axiserp.auth.ports.input.AuthenticateUserUseCase;
import com.axiserp.auth.ports.input.GetUserInfoUseCase;
import com.axiserp.auth.ports.input.RequestPasswordResetUseCase;
import com.axiserp.auth.ports.input.ResetPasswordUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GetUserInfoUseCase getUserInfoUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequestBody body,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginRequest loginRequest = new LoginRequest(body.email(), body.password());
        LoginResponse response = authenticateUserUseCase.authenticate(loginRequest, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> me(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        UserInfoResponse response = getUserInfoUseCase.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponse<Void>> passwordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        requestPasswordResetUseCase.requestReset(request.email());
        return ResponseEntity.ok(ApiResponse.ok(null,
                "Si el correo existe, recibiras un enlace de recuperacion"));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok(null, "Contraseña actualizada exitosamente"));
    }

    private static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record LoginRequestBody(@NotBlank @Email String email,
                                     @NotBlank String password) {}
}