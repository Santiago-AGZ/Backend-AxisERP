package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.auth.application.dto.request.PasswordResetRequest;
import com.axiserp.auth.application.dto.response.UserInfoResponse;
import com.axiserp.auth.infrastructure.adapters.in.web.response.ApiResponse;
import com.axiserp.auth.ports.input.GetUserInfoUseCase;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort.LoginResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GetUserInfoUseCase getUserInfoUseCase;
    private final SupabaseAuthPort supabaseAuthPort;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequestBody body) {
        LoginResponse response = supabaseAuthPort.login(body.email(), body.password());
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
        supabaseAuthPort.sendPasswordReset(request.email());
        return ResponseEntity.ok(ApiResponse.ok(null,
                "Si el correo existe, recibirás un enlace de recuperación"));
    }

    private record LoginRequestBody(@NotBlank @Email String email, @NotBlank String password) {}
}
