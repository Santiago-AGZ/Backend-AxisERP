package com.axiserp.catalog.domain.factory;

import java.time.LocalDateTime;
import java.util.UUID;

import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Category.CategoryStatus;

public final class CategoryFactory {

    private CategoryFactory() {}

    public static Category createNew(String name, String description, UUID parentId) {
        return Category.builder()
                .name(name)
                .description(description)
                .parentId(parentId)
                .status(CategoryStatus.ACTIVA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Category update(Category existing, String name, String description, UUID parentId) {
        return Category.builder()
                .id(existing.getId())
                .name(name != null ? name : existing.getName())
                .description(description != null ? description : existing.getDescription())
                .parentId(parentId != null ? parentId : existing.getParentId())
                .status(existing.getStatus())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Category deactivate(Category existing) {
        return Category.builder()
                .id(existing.getId())
                .name(existing.getName())
                .description(existing.getDescription())
                .parentId(existing.getParentId())
                .status(CategoryStatus.INACTIVA)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Category softDelete(Category existing) {
        return Category.builder()
                .id(existing.getId())
                .name(existing.getName())
                .description(existing.getDescription())
                .parentId(existing.getParentId())
                .status(CategoryStatus.ELIMINADA)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
