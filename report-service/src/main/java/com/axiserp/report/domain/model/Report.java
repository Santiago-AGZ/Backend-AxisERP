package com.axiserp.report.domain.model;
import java.util.UUID;
import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Report {
    private UUID id;
    private String name;
}