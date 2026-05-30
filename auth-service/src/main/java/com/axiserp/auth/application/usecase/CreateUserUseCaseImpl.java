package com.axiserp.auth.application.usecase;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.request.CreateUserRequest;
import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.exception.DuplicateEmailException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.CreateUserUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.SupabaseUser;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateUserUseCaseImpl.class);

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final AuditService auditService;
    private final SupabaseAuthPort supabaseAuthPort;

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request, UUID createdBy) {
        if (userRepositoryPort.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        var role = roleRepositoryPort.findByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + request.getRole()));

        SupabaseUser supabaseUser = supabaseAuthPort.createUser(
                request.getEmail(), role.getName(), request.getName(), createdBy);

        User user = UserFactory.createNew(
                supabaseUser.id(), request.getName(), request.getEmail(), role.getId(), createdBy);

        User saved = userRepositoryPort.save(user);

        auditService.log(AuditAction.CREATE, "USER", saved.getId(),
                createdBy, null,
                Map.of("email", saved.getEmail(), "role", request.getRole(), "supabaseId", supabaseUser.id()),
                null, null);

        log.info("user_created id={} email={} role={} created_by={} supabase_id={}",
                saved.getId(), saved.getEmail(), request.getRole(), createdBy, supabaseUser.id());

        return UserResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(request.getRole())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
