package com.axiserp.report.domain.model;

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
public class ExportLog {
    private UUID id;
    private UUID userId;
    private String reportType;
    private String format;
    private Integer recordCount;
    private Long fileSizeBytes;
    private LocalDateTime generatedAt;
    private String filterParams;
}
