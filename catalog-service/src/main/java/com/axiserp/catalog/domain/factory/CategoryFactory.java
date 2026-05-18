package com.axiserp.catalog.domain.factory;

import java.time.LocalDateTime;
import java.util.UUID;

import com.axiserp.catalog.domain.model.Category;

public final class CategoryFactory {

    private CategoryFactory() {}

    public static Category createNew(String name, String description) {
        return Category.builder()
                .name(name)
                .description(description)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Category update(Category existing, String name, String description) {
        return Category.builder()
                .id(existing.getId())
                .name(name != null ? name : existing.getName())
                .description(description != null ? description : existing.getDescription())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
