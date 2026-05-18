package com.axiserp.catalog.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.catalog.domain.exception.CategoryHasProductsException;
import com.axiserp.catalog.domain.exception.CategoryNotFoundException;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryUseCaseImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private DeleteCategoryUseCaseImpl deleteCategoryUseCase;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should delete category successfully when no products")
    void delete_success() {
        Category category = Category.builder()
                .id(categoryId)
                .name("Empty Category")
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepositoryPort.countByCategoryId(categoryId)).thenReturn(0);

        deleteCategoryUseCase.delete(categoryId);

        verify(categoryRepositoryPort).deleteById(categoryId);
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when not found")
    void delete_notFound() {
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> deleteCategoryUseCase.delete(categoryId));
        verify(categoryRepositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw CategoryHasProductsException when has products")
    void delete_hasProducts() {
        Category category = Category.builder()
                .id(categoryId)
                .name("Category With Products")
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepositoryPort.countByCategoryId(categoryId)).thenReturn(5);

        assertThrows(CategoryHasProductsException.class, () -> deleteCategoryUseCase.delete(categoryId));
        verify(categoryRepositoryPort, never()).deleteById(any());
    }
}
