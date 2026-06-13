package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.AuditLogEntity;

public interface JpaAuditLogRepository extends JpaRepository<AuditLogEntity, UUID>, JpaSpecificationExecutor<AuditLogEntity> {

    default Page<AuditLogEntity> findByFilters(UUID userId, AuditLogEntity.AuditAction action, String entityType, int page, int size) {
        Specification<AuditLogEntity> spec = Specification.allOf();

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (action != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(cb.lower(root.get("action")), action.name().toLowerCase()));
        }
        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, query, cb) ->
                cb.equal(cb.lower(root.get("entityType")), entityType.toLowerCase()));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return findAll(spec, pageable);
    }
}
