package com.axiserp.catalog.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.CategoryEntity;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.repository.JpaCategoryRepository;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final JpaCategoryRepository jpaCategoryRepository;

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaCategoryRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return jpaCategoryRepository.findByName(name).map(this::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaCategoryRepository.existsByName(name);
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = toEntity(category);
        CategoryEntity saved = jpaCategoryRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Category> findAllOrderedByName() {
        return jpaCategoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaCategoryRepository.deleteById(id);
    }

    private Category toDomain(CategoryEntity entity) {
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private CategoryEntity toEntity(Category domain) {
        return CategoryEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
