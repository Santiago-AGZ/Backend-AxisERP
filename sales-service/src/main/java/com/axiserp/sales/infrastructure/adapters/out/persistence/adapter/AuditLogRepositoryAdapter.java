package com.axiserp.sales.infrastructure.adapters.out.persistence.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.axiserp.sales.domain.model.AuditLog;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.AuditLogEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.repository.JpaAuditLogRepository;
import com.axiserp.sales.ports.output.AuditLogRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final JpaAuditLogRepository jpaAuditLogRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity = toEntity(auditLog);
        AuditLogEntity saved = jpaAuditLogRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AuditLog> findAll(org.springframework.data.domain.PageRequest pageRequest) {
        return jpaAuditLogRepository.findAll(
                org.springframework.data.domain.PageRequest.of(
                    pageRequest.getPageNumber(), pageRequest.getPageSize(),
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp")))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return jpaAuditLogRepository.count();
    }

    private AuditLog toDomain(AuditLogEntity e) {
        return AuditLog.builder()
                .id(e.getId())
                .action(e.getAction())
                .entityType(e.getEntityType())
                .entityId(e.getEntityId())
                .details(e.getDetails())
                .userId(e.getUserId())
                .userName(e.getUserName())
                .ipAddress(e.getIpAddress())
                .userAgent(e.getUserAgent())
                .timestamp(e.getTimestamp())
                .build();
    }

    private AuditLogEntity toEntity(AuditLog d) {
        return AuditLogEntity.builder()
                .id(d.getId() != null ? d.getId() : UUID.randomUUID())
                .action(d.getAction())
                .entityType(d.getEntityType())
                .entityId(d.getEntityId())
                .details(d.getDetails())
                .userId(d.getUserId())
                .userName(d.getUserName())
                .ipAddress(d.getIpAddress())
                .userAgent(d.getUserAgent())
                .timestamp(d.getTimestamp() != null ? d.getTimestamp() : LocalDateTime.now())
                .build();
    }
}