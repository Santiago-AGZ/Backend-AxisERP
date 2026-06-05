package com.axiserp.inventory.ports.output;

import com.axiserp.inventory.domain.model.AuditLog;

public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
}
