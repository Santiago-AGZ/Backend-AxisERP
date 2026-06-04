package com.axiserp.catalog.infrastructure.adapters.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.CatalogAuditLogEntity;

public interface JpaCatalogAuditLogRepository extends JpaRepository<CatalogAuditLogEntity, UUID> {
}
