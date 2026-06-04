package com.axiserp.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogAuditLog {

    private UUID id;
    private LocalDateTime timestamp;
    private UUID userId;
    private String action;
    private String entityType;
    private UUID entityId;
    private String detail;
    private String ipAddress;
    private String userAgent;
}
