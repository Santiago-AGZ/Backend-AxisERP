package com.axiserp.sales.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.sales.domain.model.AuditLog;
import com.axiserp.sales.ports.output.AuditLogRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public void logSaleCreated(UUID saleId, UUID userId, String userName, String details) {
        save("SALE_CREATED", "SALE", saleId, details, userId, userName);
    }

    public void logSaleConfirmed(UUID saleId, UUID userId, String userName, String details) {
        save("SALE_CONFIRMED", "SALE", saleId, details, userId, userName);
    }

    public void logSaleVoided(UUID saleId, UUID userId, String userName, String details) {
        save("SALE_VOIDED", "SALE", saleId, details, userId, userName);
    }

    public void logSalePaid(UUID saleId, UUID userId, String userName, String details) {
        save("SALE_PAID", "SALE", saleId, details, userId, userName);
    }

    private void save(String action, String entityType, UUID entityId, String details, UUID userId, String userName) {
        AuditLog auditLog = AuditLog.builder()
                .id(UUID.randomUUID())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .userId(userId)
                .userName(userName)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepositoryPort.save(auditLog);
        log.info("audit_log action={} entityType={} entityId={} userId={}", action, entityType, entityId, userId);
    }
}