package com.axiserp.purchase.ports.output;

import com.axiserp.purchase.domain.model.AuditLog;

public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
}
