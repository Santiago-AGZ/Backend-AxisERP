package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RefreshTokenEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RefreshTokenEntity.TokenStatus;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaRefreshTokenRepository;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * Adapter para RefreshTokenRepositoryPort.
 * Implementa persistencia de refresh tokens usando JPA.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final JpaRefreshTokenRepository jpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        TokenStatus status = refreshToken.isRevoked() ? TokenStatus.REVOKED : TokenStatus.ACTIVE;

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
            .id(refreshToken.getId())
            .userId(refreshToken.getUserId())
            .token(refreshToken.getToken())
            .expiresAt(refreshToken.getExpiresAt())
            .createdAt(refreshToken.getCreatedAt())
            .status(status)
            .build();

        RefreshTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
            .map(this::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByUserIdAndToken(UUID userId, String token) {
        return jpaRepository.findByUserIdAndToken(userId, token)
            .map(this::toDomain);
    }

    @Override
    public void deleteByToken(String token) {
        jpaRepository.deleteByToken(token);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteExpired() {
        jpaRepository.deleteExpired();
    }

    /**
     * Mapea RefreshTokenEntity a RefreshToken domain model.
     */
    private RefreshToken toDomain(RefreshTokenEntity entity) {
        boolean isRevoked = entity.getStatus() == TokenStatus.REVOKED;
        RefreshToken token = new RefreshToken(
            entity.getId(),
            entity.getUserId(),
            entity.getToken(),
            entity.getExpiresAt(),
            entity.getCreatedAt(),
            isRevoked
        );
        return token;
    }
}
