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
    public List<UserResponse> listAll(String role, String status, String search) {
        var users = userRepositoryPort.findAll(status, search);

        if (role != null && !role.isBlank()) {
            var roleEntity = roleRepositoryPort.findByName(role.toUpperCase());
            if (roleEntity.isPresent()) {
                var roleId = roleEntity.get().getId();
                users = users.stream()
                        .filter(u -> roleId.equals(u.getRoleId()))
                        .toList();
            } else {
                return List.of();
            }
        }

        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User user) {
        String roleName = roleRepositoryPort.findById(user.getRoleId())
                .map(r -> r.getName())
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
