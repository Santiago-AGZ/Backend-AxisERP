package com.axiserp.purchase.infrastructure.adapters.out.persistence.adapter;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.purchase.domain.model.AuditLog;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.AuditLogEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaAuditLogRepository;
import com.axiserp.purchase.ports.output.AuditLogRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final JpaAuditLogRepository jpaAuditLogRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity = toEntity(auditLog);
        AuditLogEntity saved = jpaAuditLogRepository.save(entity);
        return toDomain(saved);
    }

    private AuditLog toDomain(AuditLogEntity e) {
        return AuditLog.builder()
                .id(e.getId())
                .action(e.getAction())
                .entityType(e.getEntityType())
                .entityId(e.getEntityId())
                .detail(e.getDetail())
                .userId(e.getUserId())
                .ipAddress(e.getIpAddress())
                .userAgent(e.getUserAgent())
                .timestamp(e.getTimestamp())
                .build();
    }

    private AuditLogEntity toEntity(AuditLog d) {
        return AuditLogEntity.builder()
                .id(d.getId() != null ? d.getId() : UUID.randomUUID())
                .action(d.getAction())
                .entityType(d.getEntityType())
                .entityId(d.getEntityId())
                .detail(d.getDetail())
                .userId(d.getUserId())
                .ipAddress(d.getIpAddress())
                .userAgent(d.getUserAgent())
                .timestamp(d.getTimestamp() != null ? d.getTimestamp() : LocalDateTime.now())
                .build();
    }
}
