package com.axiserp.auth.ports.input;

import java.util.List;

import com.axiserp.auth.application.dto.response.UserResponse;

public interface ListUsersUseCase {

    List<UserResponse> listAll(String role, String status, String search);
}
