package com.axiserp.inventory.infrastructure.adapters.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.AuditLogEntity;

public interface JpaAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
}
