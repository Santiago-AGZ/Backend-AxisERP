package com.axiserp.purchase.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.AuditLogEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaAuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final JpaAuditLogRepository auditLogRepository;

    public void logCreate(String entityType, UUID entityId, UUID userId, String detail) {
        save("CREATE", entityType, entityId, userId, detail);
    }

    public void logUpdate(String entityType, UUID entityId, UUID userId, String detail) {
        save("UPDATE", entityType, entityId, userId, detail);
    }

    public void logDeactivate(String entityType, UUID entityId, UUID userId, String detail) {
        save("DEACTIVATE", entityType, entityId, userId, detail);
    }

    private void save(String action, String entityType, UUID entityId, UUID userId, String detail) {
        AuditLogEntity entry = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .detail(detail)
                .build();
        auditLogRepository.save(entry);
        log.info("audit_log action={} entity={} id={}", action, entityType, entityId);
    }
}

