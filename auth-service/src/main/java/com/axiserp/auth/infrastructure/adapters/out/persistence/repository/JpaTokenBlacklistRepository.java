package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.TokenBlacklistEntity;

public interface JpaTokenBlacklistRepository extends JpaRepository<TokenBlacklistEntity, UUID> {

    boolean existsByToken(String token);
}
