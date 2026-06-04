package com.axiserp.sales.infrastructure.adapters.out.persistence.adapter;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.sales.domain.model.AuditLog;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.AuditLogEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.repository.JpaAuditLogRepository;
import com.axiserp.sales.ports.output.AuditLogRepositoryPort;

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
                .details(e.getDetails())
                .userId(e.getUserId())
                .userName(e.getUserName())
                .timestamp(e.getTimestamp())
                .build();
    }

    private AuditLogEntity toEntity(AuditLog d) {
        return AuditLogEntity.builder()
                .id(d.getId() != null ? d.getId() : UUID.randomUUID())
                .action(d.getAction())
                .entityType(d.getEntityType())
                .entityId(d.getEntityId())
                .details(d.getDetails())
                .userId(d.getUserId())
                .userName(d.getUserName())
                .timestamp(d.getTimestamp() != null ? d.getTimestamp() : LocalDateTime.now())
                .build();
    }
}