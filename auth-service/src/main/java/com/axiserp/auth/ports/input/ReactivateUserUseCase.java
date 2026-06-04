package com.axiserp.auth.ports.input;

import java.util.UUID;

import com.axiserp.auth.application.dto.response.UserResponse;

public interface ReactivateUserUseCase {

    UserResponse reactivate(UUID id, UUID updatedBy);
}
