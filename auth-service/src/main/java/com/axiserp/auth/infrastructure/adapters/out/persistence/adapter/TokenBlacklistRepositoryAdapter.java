package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.TokenBlacklist;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.TokenBlacklistEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaTokenBlacklistRepository;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * Adapter para TokenBlacklistRepositoryPort.
 * Implementa persistencia de tokens revocados usando JPA.
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistRepositoryAdapter implements TokenBlacklistRepositoryPort {

    private final JpaTokenBlacklistRepository jpaRepository;

    @Override
    public TokenBlacklist save(TokenBlacklist tokenBlacklist) {
        TokenBlacklistEntity entity = TokenBlacklistEntity.builder()
            .id(tokenBlacklist.getId())
            .tokenJti(tokenBlacklist.getTokenJti())
            .userId(tokenBlacklist.getUserId())
            .expiresAt(tokenBlacklist.getExpiresAt())
            .createdAt(tokenBlacklist.getCreatedAt())
            .build();

        TokenBlacklistEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TokenBlacklist> findByTokenJti(String tokenJti) {
        return jpaRepository.findByTokenJti(tokenJti)
            .map(this::toDomain);
    }

    @Override
    public boolean existsByTokenJti(String tokenJti) {
        return jpaRepository.existsByTokenJti(tokenJti);
    }

    @Override
    public Optional<TokenBlacklist> findByToken(String token) {
        return jpaRepository.findByTokenJti(token)
            .map(this::toDomain);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return jpaRepository.existsByTokenJti(token);
    }

    @Override
    public void deleteExpired() {
        jpaRepository.deleteExpired();
    }

    /**
     * Mapea TokenBlacklistEntity a TokenBlacklist domain model.
     */
    private TokenBlacklist toDomain(TokenBlacklistEntity entity) {
        return new TokenBlacklist(
            entity.getId(),
            entity.getTokenJti(),
            entity.getUserId(),
            entity.getExpiresAt(),
            entity.getCreatedAt()
        );
    }
}
