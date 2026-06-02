package com.axiserp.catalog.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.catalog.application.dto.request.UpdateCategoryRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.domain.exception.CategoryNotFoundException;
import com.axiserp.catalog.domain.exception.DuplicateCategoryException;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Category.CategoryStatus;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryUseCaseImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private UpdateCategoryUseCaseImpl updateCategoryUseCase;

    private UUID categoryId;

    @Test
    @DisplayName("Should update category successfully")
    void update_success() {
        categoryId = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        Category existing = Category.builder()
                .id(categoryId)
                .name("Old Name")
                .description("Old description")
                .status(CategoryStatus.ACTIVA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Category saved = Category.builder()
                .id(categoryId)
                .name("New Name")
                .description("New description")
                .status(CategoryStatus.ACTIVA)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        UpdateCategoryRequest request = new UpdateCategoryRequest("New Name", "New description", null);

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(existing));
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = updateCategoryUseCase.update(categoryId, request, updatedBy);

        assertNotNull(response);
        assertEquals("New Name", response.getName());
        assertEquals("New description", response.getDescription());
        assertEquals("ACTIVA", response.getStatus());
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when not found")
    void update_notFound() {
        categoryId = UUID.randomUUID();
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> updateCategoryUseCase.update(categoryId, new UpdateCategoryRequest(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("Should throw DuplicateCategoryException when new name exists")
    void update_duplicateName() {
        categoryId = UUID.randomUUID();
        Category existing = Category.builder()
                .id(categoryId)
                .name("Old Name")
                .description("Old description")
                .status(CategoryStatus.ACTIVA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(existing));
        when(categoryRepositoryPort.existsByName("Existing Name")).thenReturn(true);

        UpdateCategoryRequest request = new UpdateCategoryRequest("Existing Name", null, null);

        assertThrows(DuplicateCategoryException.class, () -> updateCategoryUseCase.update(categoryId, request, UUID.randomUUID()));
    }
}
