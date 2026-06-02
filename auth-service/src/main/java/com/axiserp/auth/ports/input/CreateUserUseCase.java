package com.axiserp.auth.ports.input;

import java.util.UUID;

import com.axiserp.auth.application.dto.request.CreateUserRequest;
import com.axiserp.auth.application.dto.response.UserResponse;

public interface CreateUserUseCase {

    UserResponse create(CreateUserRequest request, UUID createdBy);
}
