package com.axiserp.auth.ports.input;

import java.util.UUID;

import com.axiserp.auth.application.dto.request.UpdateUserRequest;
import com.axiserp.auth.application.dto.response.UserResponse;

public interface UpdateUserUseCase {

    UserResponse update(UUID id, UpdateUserRequest request, UUID updatedBy);
}
