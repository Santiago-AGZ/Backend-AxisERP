package com.axiserp.catalog.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.application.dto.request.UpdateCategoryRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.domain.exception.CategoryNotFoundException;
import com.axiserp.catalog.domain.exception.DuplicateCategoryException;
import com.axiserp.catalog.domain.factory.CategoryFactory;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.ports.input.UpdateCategoryUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCaseImpl implements UpdateCategoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateCategoryUseCaseImpl.class);

    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    @Transactional
    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        Category existing = categoryRepositoryPort.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        if (existing.isDeleted()) {
            throw new IllegalStateException("No se puede modificar una categoria eliminada");
        }

        if (request.getName() != null && !request.getName().equals(existing.getName())) {
            if (categoryRepositoryPort.existsByName(request.getName())) {
                throw new DuplicateCategoryException();
            }
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("Una categoria no puede ser padre de si misma");
            }
            categoryRepositoryPort.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Categoria padre no encontrada"));
        }

        Category updated = CategoryFactory.update(existing, request.getName(), request.getDescription(), request.getParentId());
        Category saved = categoryRepositoryPort.save(updated);

        log.info("category_updated id={} name={}", saved.getId(), saved.getName());

        return toResponse(saved);
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .status(category.getStatus().name())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
