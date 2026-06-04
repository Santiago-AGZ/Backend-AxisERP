package com.axiserp.catalog.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public List<Category> findAllOrderedByName(int page, int size) {
        return jpaCategoryRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Category> findByFilters(String search, boolean includeInactive, int page, int size) {
        int offset = page * size;
        String searchParam = (search != null && !search.isBlank()) ? search : null;
        return jpaCategoryRepository.findByFilters(searchParam, includeInactive, size, offset)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByFilters(String search, boolean includeInactive) {
        String searchParam = (search != null && !search.isBlank()) ? search : null;
        return jpaCategoryRepository.countByFilters(searchParam, includeInactive);
    }

    @Override
    public long countAll() {
        return jpaCategoryRepository.count();
    }

    private Category toDomain(CategoryEntity entity) {
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .parentId(entity.getParentId())
                .status(Category.CategoryStatus.valueOf(entity.getStatus().name()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private CategoryEntity toEntity(Category domain) {
        return CategoryEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .parentId(domain.getParentId())
                .status(CategoryEntity.CategoryStatus.valueOf(domain.getStatus().name()))
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
