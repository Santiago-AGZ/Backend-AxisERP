package com.axiserp.inventory.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.AuditLogEntity;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.repository.JpaAuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final JpaAuditLogRepository auditLogRepository;

    public void logStockEntry(UUID productId, UUID userId, int quantity, int previousStock, int newStock) {
        AuditLogEntity entry = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .action("STOCK_ENTRY")
                .entityType("INVENTORY")
                .entityId(productId)
                .userId(userId)
                .detail(String.format("qty=%d prev=%d new=%d", quantity, previousStock, newStock))
                .build();
        auditLogRepository.save(entry);
        log.info("audit_log stock_entry productId={} userId={}", productId, userId);
    }

    public void logStockExit(UUID productId, UUID userId, int quantity, int previousStock, int newStock) {
        AuditLogEntity entry = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .action("STOCK_EXIT")
                .entityType("INVENTORY")
                .entityId(productId)
                .userId(userId)
                .detail(String.format("qty=%d prev=%d new=%d", quantity, previousStock, newStock))
                .build();
        auditLogRepository.save(entry);
        log.info("audit_log stock_exit productId={} userId={}", productId, userId);
    }

    public void logInitialize(UUID productId, UUID userId, int quantity) {
        AuditLogEntity entry = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .action("CREATE")
                .entityType("INVENTORY")
                .entityId(productId)
                .userId(userId)
                .detail(String.format("initial_stock=%d", quantity))
                .build();
        auditLogRepository.save(entry);
        log.info("audit_log init productId={} userId={}", productId, userId);
    }

    public void logReversal(UUID productId, UUID userId, UUID movementId, int quantity) {
        AuditLogEntity entry = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .action("REVERSAL")
                .entityType("INVENTORY")
                .entityId(productId)
                .userId(userId)
                .detail(String.format("movementId=%s qty=%d", movementId, quantity))
                .build();
        auditLogRepository.save(entry);
        log.info("audit_log reversal productId={} movementId={}", productId, movementId);
    }
}
