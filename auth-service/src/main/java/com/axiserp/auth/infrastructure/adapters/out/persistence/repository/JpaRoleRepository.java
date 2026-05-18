package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RoleEntity;

public interface JpaRoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByName(String name);
}
