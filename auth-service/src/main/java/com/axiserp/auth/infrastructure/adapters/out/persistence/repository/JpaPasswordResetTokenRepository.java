package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.PasswordResetTokenEntity;

public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    void deleteByUserId(UUID userId);
}
