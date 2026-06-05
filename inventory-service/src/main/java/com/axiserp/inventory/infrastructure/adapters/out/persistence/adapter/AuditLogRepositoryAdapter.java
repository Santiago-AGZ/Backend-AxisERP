package com.axiserp.inventory.infrastructure.adapters.out.persistence.adapter;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.axiserp.inventory.domain.model.AuditLog;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.AuditLogEntity;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.repository.JpaAuditLogRepository;
import com.axiserp.inventory.ports.output.AuditLogRepositoryPort;
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
                .detail(e.getDetail())
                .entityId(e.getEntityId())
                .entityType(e.getEntityType())
                .timestamp(e.getTimestamp())
                .userId(e.getUserId())
                .ipAddress(e.getIpAddress())
                .userAgent(e.getUserAgent())
                .build();
    }

    private AuditLogEntity toEntity(AuditLog d) {
        return AuditLogEntity.builder()
                .id(d.getId() != null ? d.getId() : UUID.randomUUID())
                .action(d.getAction())
                .detail(d.getDetail())
                .entityId(d.getEntityId())
                .entityType(d.getEntityType())
                .timestamp(d.getTimestamp() != null ? d.getTimestamp() : LocalDateTime.now())
                .userId(d.getUserId())
                .ipAddress(d.getIpAddress())
                .userAgent(d.getUserAgent())
                .build();
    }
}
