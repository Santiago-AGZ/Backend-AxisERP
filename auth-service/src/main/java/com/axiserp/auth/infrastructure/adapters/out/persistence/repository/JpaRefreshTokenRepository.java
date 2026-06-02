package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RefreshTokenEntity;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByToken(String token);

    List<RefreshTokenEntity> findByUserIdAndStatus(UUID userId, RefreshTokenEntity.TokenStatus status);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.status = 'REVOKED', r.revokedAt = CURRENT_TIMESTAMP WHERE r.userId = :userId AND r.status = 'ACTIVE'")
    void revokeByUserId(@Param("userId") UUID userId);
}
