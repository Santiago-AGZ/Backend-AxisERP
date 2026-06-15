package com.axiserp.catalog.domain.factory;

import java.time.LocalDateTime;
import java.util.UUID;

import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Category.CategoryStatus;

@Deprecated
public final class CategoryFactory {

    private CategoryFactory() {}

    public static Category createNew(String name, String description, UUID parentId, UUID createdBy) {
        return Category.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .parentId(parentId)
                .status(CategoryStatus.ACTIVA)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Category update(Category existing, String name, String description, UUID parentId, UUID updatedBy) {
        return Category.builder()
                .id(existing.getId())
                .name(name != null ? name : existing.getName())
                .description(description != null ? description : existing.getDescription())
                .parentId(parentId != null ? parentId : existing.getParentId())
                .status(existing.getStatus())
                .createdBy(existing.getCreatedBy())
                .updatedBy(updatedBy)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Category reactivate(Category existing, UUID updatedBy) {
        return Category.builder()
                .id(existing.getId())
                .name(existing.getName())
                .description(existing.getDescription())
                .parentId(existing.getParentId())
                .status(CategoryStatus.ACTIVA)
                .createdBy(existing.getCreatedBy())
                .updatedBy(updatedBy)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Category deactivate(Category existing, UUID updatedBy) {
        return Category.builder()
                .id(existing.getId())
                .name(existing.getName())
                .description(existing.getDescription())
                .parentId(existing.getParentId())
                .status(CategoryStatus.INACTIVA)
                .createdBy(existing.getCreatedBy())
                .updatedBy(updatedBy)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Category softDelete(Category existing, UUID updatedBy) {
        return Category.builder()
                .id(existing.getId())
                .name(existing.getName())
                .description(existing.getDescription())
                .parentId(existing.getParentId())
                .status(CategoryStatus.ELIMINADA)
                .createdBy(existing.getCreatedBy())
                .updatedBy(updatedBy)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
