package com.axiserp.catalog.domain.model;

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
public class Category {

    private UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private CategoryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum CategoryStatus {
        ACTIVA, INACTIVA, ELIMINADA
    }

    public boolean isActive() {
        return this.status == CategoryStatus.ACTIVA;
    }

    public boolean isDeleted() {
        return this.status == CategoryStatus.ELIMINADA;
    }
}
