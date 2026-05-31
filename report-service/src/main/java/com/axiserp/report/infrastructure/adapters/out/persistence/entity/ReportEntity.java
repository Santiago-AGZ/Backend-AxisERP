package com.axiserp.report.infrastructure.adapters.out.persistence.entity;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "report_cache")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "cache_key") private String name;
    @Column(name = "cache_data") private String data;
}