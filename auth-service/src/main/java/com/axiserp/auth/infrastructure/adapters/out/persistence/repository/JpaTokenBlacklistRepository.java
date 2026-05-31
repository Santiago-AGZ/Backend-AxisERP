package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.TokenBlacklistEntity;

@Repository
public interface JpaTokenBlacklistRepository extends JpaRepository<TokenBlacklistEntity, UUID> {
    Optional<TokenBlacklistEntity> findByTokenJti(String tokenJti);
    boolean existsByTokenJti(String tokenJti);

    @Modifying
    @Query("DELETE FROM TokenBlacklistEntity t WHERE t.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpired();
}
