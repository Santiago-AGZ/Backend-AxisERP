package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.auth.application.dto.request.CreateUserRequest;
import com.axiserp.auth.application.dto.request.LogoutRequest;
import com.axiserp.auth.application.dto.request.PasswordResetRequest;
import com.axiserp.auth.application.dto.request.ResetPasswordRequest;
import com.axiserp.auth.application.dto.request.UpdateUserRequest;
import com.axiserp.auth.application.dto.response.AuditLogResponse;
import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.ports.input.CreateUserUseCase;
import com.axiserp.auth.ports.input.DeactivateUserUseCase;
import com.axiserp.auth.ports.input.GetAuditLogUseCase;
import com.axiserp.auth.ports.input.GetProfileUseCase;
import com.axiserp.auth.ports.input.GetUserUseCase;
import com.axiserp.auth.ports.input.ListUsersUseCase;
import com.axiserp.auth.ports.input.LogoutUseCase;
import com.axiserp.auth.ports.input.RequestPasswordResetUseCase;
import com.axiserp.auth.ports.input.ResetPasswordUseCase;
import com.axiserp.auth.ports.input.UpdateUserUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final GetAuditLogUseCase getAuditLogUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/usuarios")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {

        UUID adminId = UUID.fromString((String) authentication.getPrincipal());
        UserResponse response = createUserUseCase.create(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(listUsersUseCase.listAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(getUserUseCase.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(updateUserUseCase.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/usuarios/{id}/desactivar")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(deactivateUserUseCase.deactivate(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/usuarios/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(getProfileUseCase.getProfile(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit-log")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getAuditLogUseCase.getAuditLogs(userId, action, entityType, page, size));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/recuperar-password")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        requestPasswordResetUseCase.requestReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
