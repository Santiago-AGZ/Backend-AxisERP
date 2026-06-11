package com.axiserp.auth.ports.input;

import java.util.UUID;
import com.axiserp.auth.application.dto.request.CreateRoleRequest;
import com.axiserp.auth.application.dto.response.RoleResponse;

public interface CreateRoleUseCase {
    RoleResponse create(CreateRoleRequest request, UUID createdBy);
}
