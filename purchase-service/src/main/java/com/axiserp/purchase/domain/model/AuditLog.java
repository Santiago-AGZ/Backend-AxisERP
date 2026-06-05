package com.axiserp.purchase.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private UUID id;
    private String action;
    private String entityType;
    private UUID entityId;
    private String detail;
    private UUID userId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
}
