package com.axiserp.auth.ports.input;

import java.util.UUID;

import com.axiserp.auth.application.dto.response.UserResponse;

public interface DeleteUserUseCase {

    UserResponse delete(UUID id, UUID updatedBy, String currentPassword);
}
