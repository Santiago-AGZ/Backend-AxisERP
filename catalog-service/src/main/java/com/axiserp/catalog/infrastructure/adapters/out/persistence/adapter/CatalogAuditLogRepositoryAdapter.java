package com.axiserp.catalog.infrastructure.adapters.out.persistence.adapter;

import org.springframework.stereotype.Component;

import com.axiserp.catalog.domain.model.CatalogAuditLog;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.CatalogAuditLogEntity;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.repository.JpaCatalogAuditLogRepository;
import com.axiserp.catalog.ports.output.CatalogAuditLogRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatalogAuditLogRepositoryAdapter implements CatalogAuditLogRepositoryPort {

    private final JpaCatalogAuditLogRepository jpaCatalogAuditLogRepository;

    @Override
    public CatalogAuditLog save(CatalogAuditLog auditLog) {
        CatalogAuditLogEntity entity = toEntity(auditLog);
        CatalogAuditLogEntity saved = jpaCatalogAuditLogRepository.save(entity);
        return toDomain(saved);
    }

    private CatalogAuditLog toDomain(CatalogAuditLogEntity entity) {
        return CatalogAuditLog.builder()
                .id(entity.getId())
                .timestamp(entity.getTimestamp())
                .userId(entity.getUserId())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .detail(entity.getDetail())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .build();
    }

    private CatalogAuditLogEntity toEntity(CatalogAuditLog domain) {
        return CatalogAuditLogEntity.builder()
                .id(domain.getId())
                .timestamp(domain.getTimestamp())
                .userId(domain.getUserId())
                .action(domain.getAction())
                .entityType(domain.getEntityType())
                .entityId(domain.getEntityId())
                .detail(domain.getDetail())
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .build();
    }
}
