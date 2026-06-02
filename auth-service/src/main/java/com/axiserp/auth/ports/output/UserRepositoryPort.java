package com.axiserp.auth.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.auth.domain.model.User;

/**
 * Port de salida para operaciones de persistencia de usuarios.
 * Define el contrato entre el dominio y la capa de infraestructura
 * para acceso a datos de usuarios.
 */
public interface UserRepositoryPort {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailOrName(String identifier);

    Optional<User> findById(UUID id);

    boolean existsByEmail(String email);

    User save(User user);

    List<User> findAll();
}
