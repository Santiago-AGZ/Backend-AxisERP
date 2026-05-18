package com.axiserp.auth.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.GetProfileUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProfileUseCaseImpl implements GetProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    public UserResponse getProfile(String userId) {
        User user = userRepositoryPort.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String roleName = roleRepositoryPort.findById(user.getRoleId())
                .map(role -> role.getName())
                .orElse("UNKNOWN");

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(roleName)
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
