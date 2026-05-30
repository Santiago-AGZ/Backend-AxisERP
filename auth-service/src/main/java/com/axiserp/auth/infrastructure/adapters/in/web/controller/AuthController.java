package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import jakarta.validation.Valid;

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
import com.axiserp.auth.ports.input.GetUserInfoUseCase;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GetUserInfoUseCase getUserInfoUseCase;
    private final SupabaseAuthPort supabaseAuthPort;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        UserInfoResponse response = getUserInfoUseCase.getUserInfo(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, Object>> passwordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        supabaseAuthPort.sendPasswordReset(request.email());
        return ResponseEntity.ok(Map.of(
                "message", "Si el correo existe, recibirás un enlace de recuperación"));
    }
}
