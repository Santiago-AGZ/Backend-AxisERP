package com.axiserp.auth.ports.input;

import java.util.UUID;
import com.axiserp.auth.application.dto.response.RoleResponse;

public interface GetRoleUseCase {
    RoleResponse getById(UUID id);
}
