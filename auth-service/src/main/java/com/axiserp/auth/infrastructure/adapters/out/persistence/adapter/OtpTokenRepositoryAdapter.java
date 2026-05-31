package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.OtpToken;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.OtpTokenEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaOtpTokenRepository;
import com.axiserp.auth.ports.output.OtpTokenRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * Adapter para OtpTokenRepositoryPort.
 * Implementa persistencia de OTP tokens usando JPA.
 */
@Component
@RequiredArgsConstructor
public class OtpTokenRepositoryAdapter implements OtpTokenRepositoryPort {

    private final JpaOtpTokenRepository jpaRepository;

    @Override
    public OtpToken save(OtpToken otpToken) {
        OtpTokenEntity entity = OtpTokenEntity.builder()
            .id(otpToken.getId())
            .userId(otpToken.getUserId())
            .otpCode(otpToken.getToken())
            .expiresAt(otpToken.getExpiresAt())
            .createdAt(otpToken.getCreatedAt())
            .attempts(0)
            .usedAt(otpToken.isUsed() ? otpToken.getCreatedAt() : null)
            .build();

        OtpTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OtpToken> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId)
            .map(this::toDomain);
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
     * Mapea OtpTokenEntity a OtpToken domain model.
     */
    private OtpToken toDomain(OtpTokenEntity entity) {
        OtpToken token = new OtpToken(
            entity.getId(),
            entity.getUserId(),
            entity.getOtpCode(),
            entity.getExpiresAt(),
            entity.getCreatedAt(),
            entity.getUsedAt() != null
        );
        return token;
    }
}
