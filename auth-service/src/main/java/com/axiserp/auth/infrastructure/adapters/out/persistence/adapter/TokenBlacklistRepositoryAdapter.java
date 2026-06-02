package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.TokenBlacklist;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.TokenBlacklistEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaTokenBlacklistRepository;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenBlacklistRepositoryAdapter implements TokenBlacklistRepositoryPort {

    private final JpaTokenBlacklistRepository jpaTokenBlacklistRepository;

    @Override
    public boolean isTokenBlacklisted(String token) {
        return jpaTokenBlacklistRepository.existsByToken(token);
    }

    @Override
    public TokenBlacklist save(TokenBlacklist tokenBlacklist) {
        TokenBlacklistEntity entity = toEntity(tokenBlacklist);
        TokenBlacklistEntity saved = jpaTokenBlacklistRepository.save(entity);
        return toDomain(saved);
    }

    private TokenBlacklist toDomain(TokenBlacklistEntity entity) {
        return TokenBlacklist.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .tokenType(entity.getTokenType())
                .userId(entity.getUserId())
                .reason(entity.getReason())
                .expiresAt(entity.getExpiresAt())
                .blacklistedAt(entity.getBlacklistedAt())
                .build();
    }

    private TokenBlacklistEntity toEntity(TokenBlacklist domain) {
        return TokenBlacklistEntity.builder()
                .id(domain.getId())
                .token(domain.getToken())
                .tokenType(domain.getTokenType())
                .userId(domain.getUserId())
                .reason(domain.getReason())
                .expiresAt(domain.getExpiresAt())
                .blacklistedAt(domain.getBlacklistedAt())
                .build();
    }
}
