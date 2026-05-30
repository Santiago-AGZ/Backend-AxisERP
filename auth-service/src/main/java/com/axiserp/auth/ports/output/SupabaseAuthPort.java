package com.axiserp.auth.ports.output;

import java.util.UUID;

public interface SupabaseAuthPort {
    SupabaseUser createUser(String email, String roleName, String name, UUID createdBy);
}
