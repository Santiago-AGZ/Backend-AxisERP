package com.axiserp.auth.ports.input;

import java.util.UUID;
import com.axiserp.auth.application.dto.request.UpdateRoleRequest;
import com.axiserp.auth.application.dto.response.RoleResponse;

public interface UpdateRoleUseCase {
    RoleResponse update(UUID id, UpdateRoleRequest request, UUID updatedBy);
}
