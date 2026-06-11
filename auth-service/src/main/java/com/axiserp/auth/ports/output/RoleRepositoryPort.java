package com.axiserp.auth.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.auth.domain.model.Role;

public interface RoleRepositoryPort {

    Optional<Role> findById(UUID id);

    Optional<Role> findByName(String name);

    List<Role> findAll();

    Role save(Role role);

    void deleteById(UUID id);

    boolean existsByName(String name);
}
