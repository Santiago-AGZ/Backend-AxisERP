package com.axiserp.auth.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.GetUserUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetUserUseCaseImpl implements GetUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    public UserResponse getById(UUID id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (user.getStatus() == User.UserStatus.ELIMINADO
                || user.getStatus() == User.UserStatus.INACTIVO) {
            throw new UserNotFoundException("Usuario no encontrado");
        }

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
