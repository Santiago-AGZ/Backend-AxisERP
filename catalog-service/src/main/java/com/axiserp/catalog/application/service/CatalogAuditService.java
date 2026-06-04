package com.axiserp.catalog.application.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.catalog.application.shared.RequestContext;
import com.axiserp.catalog.domain.model.CatalogAuditLog;
import com.axiserp.catalog.ports.output.CatalogAuditLogRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogAuditService {

    private static final Logger log = LoggerFactory.getLogger(CatalogAuditService.class);

    private final CatalogAuditLogRepositoryPort catalogAuditLogRepositoryPort;

    public void log(String action, String entityType, UUID entityId, UUID userId, String detail) {
        CatalogAuditLog auditLog = CatalogAuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .detail(detail)
                .ipAddress(RequestContext.getIpAddress())
                .userAgent(RequestContext.getUserAgent())
                .build();
        catalogAuditLogRepositoryPort.save(auditLog);
        log.info("catalog_audit_log action={} entity_type={} entity_id={} user_id={}", action, entityType, entityId, userId);
    }
}
