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
import com.axiserp.auth.application.dto.request.UpdateUserRequest;
import com.axiserp.auth.application.dto.response.AuditLogResponse;
import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.domain.model.PageResult;
import com.axiserp.auth.infrastructure.adapters.in.web.response.ApiResponse;
import com.axiserp.auth.ports.input.CreateUserUseCase;
import com.axiserp.auth.ports.input.DeactivateUserUseCase;
import com.axiserp.auth.ports.input.GetAuditLogUseCase;
import com.axiserp.auth.ports.input.GetProfileUseCase;
import com.axiserp.auth.ports.input.GetUserUseCase;
import com.axiserp.auth.ports.input.ListUsersUseCase;
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
    private final GetProfileUseCase getProfileUseCase;
    private final GetAuditLogUseCase getAuditLogUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/usuarios")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {

        UUID adminId = UUID.fromString((String) authentication.getPrincipal());
        UserResponse response = createUserUseCase.create(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(listUsersUseCase.listAll(role, status, search)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(getUserUseCase.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        UUID adminId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(updateUserUseCase.update(id, request, adminId), "Usuario actualizado exitosamente"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/usuarios/{id}/desactivar")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID adminId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(deactivateUserUseCase.deactivate(id, adminId), "Usuario desactivado exitosamente"));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/usuarios/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(getProfileUseCase.getProfile(userId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit-log")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<AuditLogResponse> result = getAuditLogUseCase.getAuditLogs(userId, action, entityType, page, size);
        ApiResponse<List<AuditLogResponse>> response = ApiResponse.ok(result.getContent());
        response.setPagination(result.getPage(), result.getSize(), result.getTotalElements(), result.getTotalPages());
        return ResponseEntity.ok(response);
    }
}
