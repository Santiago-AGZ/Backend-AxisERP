package com.axiserp.auth.ports.input;

import java.util.UUID;

public interface DeleteRoleUseCase {
    void delete(UUID id);
}
