package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.PasswordHistory;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.PasswordHistoryEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaPasswordHistoryRepository;
import com.axiserp.auth.ports.output.PasswordHistoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PasswordHistoryRepositoryAdapter implements PasswordHistoryRepositoryPort {

    private final JpaPasswordHistoryRepository jpaPasswordHistoryRepository;

    @Override
    public List<PasswordHistory> findLastByUserId(UUID userId, int limit) {
        return jpaPasswordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PasswordHistory save(PasswordHistory passwordHistory) {
        PasswordHistoryEntity entity = toEntity(passwordHistory);
        PasswordHistoryEntity saved = jpaPasswordHistoryRepository.save(entity);
        return toDomain(saved);
    }

    private PasswordHistory toDomain(PasswordHistoryEntity entity) {
        return PasswordHistory.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .passwordHash(entity.getPasswordHash())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private PasswordHistoryEntity toEntity(PasswordHistory domain) {
        return PasswordHistoryEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .passwordHash(domain.getPasswordHash())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
