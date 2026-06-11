package com.axiserp.auth.ports.input;

import java.util.List;
import com.axiserp.auth.application.dto.response.RoleResponse;

public interface ListRolesUseCase {
    List<RoleResponse> listAll();
}
