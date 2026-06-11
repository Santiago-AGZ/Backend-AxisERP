package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.auth.application.dto.request.CreateRoleRequest;
import com.axiserp.auth.application.dto.request.UpdateRoleRequest;
import com.axiserp.auth.application.dto.response.RoleResponse;
import com.axiserp.auth.infrastructure.adapters.in.web.response.ApiResponse;
import com.axiserp.auth.ports.input.CreateRoleUseCase;
import com.axiserp.auth.ports.input.DeleteRoleUseCase;
import com.axiserp.auth.ports.input.GetRoleUseCase;
import com.axiserp.auth.ports.input.ListRolesUseCase;
import com.axiserp.auth.ports.input.UpdateRoleUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RoleController {

    private final ListRolesUseCase listRolesUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final CreateRoleUseCase createRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.ok(listRolesUseCase.listAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(getRoleUseCase.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request,
            Authentication authentication) {
        UUID adminId = UUID.fromString((String) authentication.getPrincipal());
        RoleResponse response = createRoleUseCase.create(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication) {
        UUID adminId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(updateRoleUseCase.update(id, request, adminId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        deleteRoleUseCase.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Rol eliminado exitosamente"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<String>>> listPermissions() {
        return ResponseEntity.ok(ApiResponse.ok(Collections.emptyList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<List<String>>> getRolePermissions(@PathVariable UUID roleId) {
        return ResponseEntity.ok(ApiResponse.ok(Collections.emptyList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Void>> assignPermissions(@PathVariable UUID roleId) {
        return ResponseEntity.ok(ApiResponse.ok(null, "Funcionalidad no implementada"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/roles/{roleId}/permissions/{permId}")
    public ResponseEntity<ApiResponse<Void>> removePermission(@PathVariable UUID roleId, @PathVariable UUID permId) {
        return ResponseEntity.ok(ApiResponse.ok(null, "Funcionalidad no implementada"));
    }
}
