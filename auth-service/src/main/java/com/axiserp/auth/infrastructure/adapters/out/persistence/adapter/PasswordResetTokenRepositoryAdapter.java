package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.PasswordResetToken;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.PasswordResetTokenEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaPasswordResetTokenRepository;
import com.axiserp.auth.ports.output.PasswordResetTokenRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final JpaPasswordResetTokenRepository jpaPasswordResetTokenRepository;

    @Override
    public PasswordResetToken findByToken(String token) {
        return jpaPasswordResetTokenRepository.findByToken(token)
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken passwordResetToken) {
        PasswordResetTokenEntity entity = toEntity(passwordResetToken);
        PasswordResetTokenEntity saved = jpaPasswordResetTokenRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaPasswordResetTokenRepository.deleteByUserId(userId);
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return PasswordResetToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .token(entity.getToken())
                .used(entity.isUsed())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .usedAt(entity.getUsedAt())
                .ipAddress(entity.getIpAddress())
                .build();
    }

    private PasswordResetTokenEntity toEntity(PasswordResetToken domain) {
        return PasswordResetTokenEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .token(domain.getToken())
                .used(domain.isUsed())
                .expiresAt(domain.getExpiresAt())
                .createdAt(domain.getCreatedAt())
                .usedAt(domain.getUsedAt())
                .ipAddress(domain.getIpAddress())
                .build();
    }
}
