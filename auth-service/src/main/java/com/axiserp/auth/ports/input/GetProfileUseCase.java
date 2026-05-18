package com.axiserp.auth.ports.input;

import com.axiserp.auth.application.dto.response.UserResponse;

public interface GetProfileUseCase {

    UserResponse getProfile(String userId);
}
