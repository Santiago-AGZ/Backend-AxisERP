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

import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.domain.exception.CategoryHasProductsException;
import com.axiserp.catalog.domain.exception.CategoryNotFoundException;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Category.CategoryStatus;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

@ExtendWith(MockitoExtension.class)
class DeactivateCategoryUseCaseImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private DeactivateCategoryUseCaseImpl deactivateCategoryUseCase;

    private UUID categoryId;

    @Test
    @DisplayName("Should deactivate category successfully")
    void deactivate_success() {
        categoryId = UUID.randomUUID();
        Category existing = Category.builder()
                .id(categoryId)
                .name("Test Category")
                .description("Test description")
                .status(CategoryStatus.ACTIVA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Category deactivated = Category.builder()
                .id(categoryId)
                .name("Test Category")
                .description("Test description")
                .status(CategoryStatus.INACTIVA)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(existing));
        when(productRepositoryPort.countActiveByCategoryId(categoryId)).thenReturn(0);
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(deactivated);

        CategoryResponse response = deactivateCategoryUseCase.deactivate(categoryId, UUID.randomUUID());

        assertNotNull(response);
        assertEquals("INACTIVA", response.getStatus());
        verify(categoryRepositoryPort).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw CategoryHasProductsException when has active products")
    void deactivate_hasActiveProducts() {
        categoryId = UUID.randomUUID();
        Category existing = Category.builder()
                .id(categoryId)
                .name("Test Category")
                .description("Test description")
                .status(CategoryStatus.ACTIVA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(existing));
        when(productRepositoryPort.countActiveByCategoryId(categoryId)).thenReturn(3);

        CategoryHasProductsException exception = assertThrows(CategoryHasProductsException.class, () -> deactivateCategoryUseCase.deactivate(categoryId, UUID.randomUUID()));

        assertTrue(exception.getMessage().contains("3 producto(s) activo(s)"));
        verify(categoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when not found")
    void deactivate_notFound() {
        categoryId = UUID.randomUUID();
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> deactivateCategoryUseCase.deactivate(categoryId, UUID.randomUUID()));
        verify(categoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when already deactivated")
    void deactivate_alreadyInactive() {
        categoryId = UUID.randomUUID();
        Category existing = Category.builder()
                .id(categoryId)
                .name("Test Category")
                .status(CategoryStatus.INACTIVA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> deactivateCategoryUseCase.deactivate(categoryId, UUID.randomUUID()));
        verify(categoryRepositoryPort, never()).save(any());
    }
}
