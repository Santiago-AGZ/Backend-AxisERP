package com.axiserp.catalog.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.application.dto.request.CreateCategoryRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.domain.exception.DuplicateCategoryException;
import com.axiserp.catalog.domain.factory.CategoryFactory;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.ports.input.CreateCategoryUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCaseImpl implements CreateCategoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCategoryUseCaseImpl.class);

    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepositoryPort.existsByName(request.getName())) {
            throw new DuplicateCategoryException();
        }

        Category category = CategoryFactory.createNew(request.getName(), request.getDescription());
        Category saved = categoryRepositoryPort.save(category);

        log.info("category_created id={} name={}", saved.getId(), saved.getName());

        return toResponse(saved);
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
