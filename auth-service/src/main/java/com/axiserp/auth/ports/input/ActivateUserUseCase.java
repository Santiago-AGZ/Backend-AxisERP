package com.axiserp.auth.ports.input;

import java.util.UUID;

import com.axiserp.auth.application.dto.response.UserResponse;

public interface ActivateUserUseCase {

    UserResponse activate(UUID id, UUID updatedBy);
}
