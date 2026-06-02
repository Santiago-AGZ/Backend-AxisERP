package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.AuditLog;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.AuditLogEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaAuditLogRepository;
import com.axiserp.auth.ports.output.AuditLogRepositoryPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final JpaAuditLogRepository jpaAuditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity = toEntity(auditLog);
        AuditLogEntity saved = jpaAuditLogRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AuditLog> findByFilters(UUID userId, String action, String entityType, int page, int size) {
        AuditLogEntity.AuditAction actionEnum = action != null ? AuditLogEntity.AuditAction.valueOf(action) : null;
        return jpaAuditLogRepository.findByFilters(userId, actionEnum, entityType, page, size)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AuditLog toDomain(AuditLogEntity entity) {
        try {
            return AuditLog.builder()
                    .id(entity.getId())
                    .timestamp(entity.getTimestamp())
                    .userId(entity.getUserId())
                    .userName(entity.getUserName())
                    .action(AuditLog.AuditAction.valueOf(entity.getAction().name()))
                    .entityType(entity.getEntityType())
                    .entityId(entity.getEntityId())
                    .detail(entity.getDetail() != null
                            ? objectMapper.readValue(entity.getDetail(),
                                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {})
                            : null)
                    .ipAddress(entity.getIpAddress())
                    .userAgent(entity.getUserAgent())
                    .build();
        } catch (JsonProcessingException e) {
            return AuditLog.builder()
                    .id(entity.getId())
                    .timestamp(entity.getTimestamp())
                    .userId(entity.getUserId())
                    .userName(entity.getUserName())
                    .action(AuditLog.AuditAction.valueOf(entity.getAction().name()))
                    .entityType(entity.getEntityType())
                    .entityId(entity.getEntityId())
                    .ipAddress(entity.getIpAddress())
                    .userAgent(entity.getUserAgent())
                    .build();
        }
    }

    private AuditLogEntity toEntity(AuditLog domain) {
        String detailJson = null;
        try {
            if (domain.getDetail() != null) {
                detailJson = objectMapper.writeValueAsString(domain.getDetail());
            }
        } catch (JsonProcessingException e) {
            detailJson = "{}";
        }
        return AuditLogEntity.builder()
                .id(domain.getId())
                .timestamp(domain.getTimestamp())
                .userId(domain.getUserId())
                .userName(domain.getUserName())
                .action(AuditLogEntity.AuditAction.valueOf(domain.getAction().name()))
                .entityType(domain.getEntityType())
                .entityId(domain.getEntityId())
                .detail(detailJson)
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .build();
    }
}
