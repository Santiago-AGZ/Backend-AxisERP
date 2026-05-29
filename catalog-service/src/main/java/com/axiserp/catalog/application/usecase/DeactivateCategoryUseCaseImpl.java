package com.axiserp.catalog.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.domain.exception.CategoryHasProductsException;
import com.axiserp.catalog.domain.exception.CategoryNotFoundException;
import com.axiserp.catalog.domain.factory.CategoryFactory;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.ports.input.DeactivateCategoryUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeactivateCategoryUseCaseImpl implements DeactivateCategoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeactivateCategoryUseCaseImpl.class);

    private final CategoryRepositoryPort categoryRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    @Override
    @Transactional
    public CategoryResponse deactivate(UUID id) {
        Category existing = categoryRepositoryPort.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        if (existing.isDeleted()) {
            throw new IllegalStateException("La categoria ya esta eliminada");
        }

        int activeProductCount = productRepositoryPort.countActiveByCategoryId(id);
        if (activeProductCount > 0) {
            throw new CategoryHasProductsException(activeProductCount);
        }

        Category deleted = CategoryFactory.softDelete(existing);
        Category saved = categoryRepositoryPort.save(deleted);

        log.info("category_deleted id={} name={}", saved.getId(), saved.getName());

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
