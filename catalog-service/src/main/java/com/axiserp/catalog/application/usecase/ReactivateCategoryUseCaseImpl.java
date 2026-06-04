package com.axiserp.catalog.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.application.service.CatalogAuditService;
import com.axiserp.catalog.domain.exception.CategoryNotFoundException;
import com.axiserp.catalog.domain.factory.CategoryFactory;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.ports.input.ReactivateCategoryUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReactivateCategoryUseCaseImpl implements ReactivateCategoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReactivateCategoryUseCaseImpl.class);

    private final CategoryRepositoryPort categoryRepositoryPort;
    private final CatalogAuditService catalogAuditService;

    @Override
    @Transactional
    public CategoryResponse reactivate(UUID id, UUID updatedBy) {
        Category existing = categoryRepositoryPort.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        if (existing.isDeleted()) {
            throw new IllegalStateException("No se puede reactivar una categoria eliminada");
        }

        if (existing.isActive()) {
            throw new IllegalStateException("La categoria ya esta activa");
        }

        Category reactivated = CategoryFactory.reactivate(existing, updatedBy);
        Category saved = categoryRepositoryPort.save(reactivated);

        log.info("category_reactivated id={} name={} updatedBy={}", saved.getId(), saved.getName(), updatedBy);
        catalogAuditService.log("REACTIVATE", "CATEGORY", saved.getId(), updatedBy, "Categoria reactivada: " + saved.getName());

        return toResponse(saved);
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .status(category.getStatus().name())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
