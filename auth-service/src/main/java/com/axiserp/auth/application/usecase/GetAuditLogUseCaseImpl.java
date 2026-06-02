package com.axiserp.auth.application.usecase;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.axiserp.auth.application.dto.response.AuditLogResponse;
import com.axiserp.auth.domain.model.AuditLog;
import com.axiserp.auth.ports.input.GetAuditLogUseCase;
import com.axiserp.auth.ports.output.AuditLogRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAuditLogUseCaseImpl implements GetAuditLogUseCase {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    @Override
    public List<AuditLogResponse> getAuditLogs(UUID userId, String action, String entityType, int page, int size) {
        List<AuditLog> logs = auditLogRepositoryPort.findByFilters(userId, action, entityType, page, size);
        return logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .timestamp(log.getTimestamp())
                .userId(log.getUserId())
                .userName(log.getUserName())
                .action(log.getAction().name())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .detail(log.getDetail())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .build();
    }
}
