package com.axiserp.sales.ports.output;

import java.util.List;

import org.springframework.data.domain.PageRequest;

import com.axiserp.sales.domain.model.AuditLog;

public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
    List<AuditLog> findAll(PageRequest pageRequest);
    long count();
}