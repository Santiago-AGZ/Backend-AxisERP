package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.OtpTokenEntity;

@Repository
public interface JpaOtpTokenRepository extends JpaRepository<OtpTokenEntity, UUID> {
    Optional<OtpTokenEntity> findByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM OtpTokenEntity o WHERE o.userId = :userId")
    void deleteByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM OtpTokenEntity o WHERE o.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpired();
}
