package com.axiserp.sales.ports.output;

import com.axiserp.sales.domain.model.AuditLog;

public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
}