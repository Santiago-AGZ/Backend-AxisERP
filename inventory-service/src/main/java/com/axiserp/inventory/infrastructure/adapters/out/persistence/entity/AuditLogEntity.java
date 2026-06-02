package com.axiserp.inventory.infrastructure.adapters.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "user_id")
    private UUID userId;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
