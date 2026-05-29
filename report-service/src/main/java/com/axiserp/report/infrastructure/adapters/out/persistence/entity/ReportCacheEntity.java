package com.axiserp.report.infrastructure.adapters.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "report_cache")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Column(name = "cache_key", nullable = false, unique = true, length = 255)
    private String cacheKey;

    @Column(nullable = false, columnDefinition = "JSONB")
    private String data;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "generated_by")
    private UUID generatedBy;

    @Column(name = "filter_params", columnDefinition = "JSONB")
    private String filterParams;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = generatedAt.plusMinutes(5);
        }
    }
}
