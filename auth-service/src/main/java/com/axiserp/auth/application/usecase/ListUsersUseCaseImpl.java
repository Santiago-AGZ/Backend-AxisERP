package com.axiserp.auth.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.ListUsersUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListUsersUseCaseImpl implements ListUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    public List<UserResponse> listAll() {
        return userRepositoryPort.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User user) {
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
