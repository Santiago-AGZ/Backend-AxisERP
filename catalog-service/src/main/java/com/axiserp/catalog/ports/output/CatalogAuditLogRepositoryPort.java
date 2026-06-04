package com.axiserp.catalog.ports.output;

import com.axiserp.catalog.domain.model.CatalogAuditLog;

public interface CatalogAuditLogRepositoryPort {

    CatalogAuditLog save(CatalogAuditLog auditLog);
}
