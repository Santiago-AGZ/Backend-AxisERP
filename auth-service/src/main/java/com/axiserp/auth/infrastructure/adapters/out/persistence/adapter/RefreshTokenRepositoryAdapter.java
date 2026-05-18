package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RefreshTokenEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaRefreshTokenRepository;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRefreshTokenRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    public List<RefreshToken> findActiveByUserId(UUID userId) {
        return jpaRefreshTokenRepository.findByUserIdAndStatus(userId, RefreshTokenEntity.TokenStatus.ACTIVE)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = toEntity(refreshToken);
        RefreshTokenEntity saved = jpaRefreshTokenRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void revokeByUserId(UUID userId) {
        jpaRefreshTokenRepository.revokeByUserId(userId);
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return RefreshToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .token(entity.getToken())
                .status(RefreshToken.TokenStatus.valueOf(entity.getStatus().name()))
                .expiresAt(entity.getExpiresAt())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .revokedAt(entity.getRevokedAt())
                .build();
    }

    private RefreshTokenEntity toEntity(RefreshToken domain) {
        return RefreshTokenEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .token(domain.getToken())
                .status(RefreshTokenEntity.TokenStatus.valueOf(domain.getStatus().name()))
                .expiresAt(domain.getExpiresAt())
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .createdAt(domain.getCreatedAt())
                .revokedAt(domain.getRevokedAt())
                .build();
    }
}
