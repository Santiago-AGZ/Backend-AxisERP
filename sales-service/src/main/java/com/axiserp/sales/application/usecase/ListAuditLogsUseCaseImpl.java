package com.axiserp.sales.application.usecase;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.AuditLogResponse;
import com.axiserp.sales.application.dto.response.PaginatedResponse;
import com.axiserp.sales.ports.input.ListAuditLogsUseCase;
import com.axiserp.sales.ports.output.AuditLogRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListAuditLogsUseCaseImpl implements ListAuditLogsUseCase {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<AuditLogResponse> list(int page, int size) {
        var pageRequest = PageRequest.of(page, size);
        List<AuditLogResponse> content = auditLogRepositoryPort.findAll(pageRequest)
                .stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getDetails(),
                        log.getUserId(),
                        log.getUserName(),
                        log.getIpAddress(),
                        log.getUserAgent(),
                        log.getTimestamp()))
                .toList();
        long total = auditLogRepositoryPort.count();
        return new PaginatedResponse<>(content, total);
    }
}
