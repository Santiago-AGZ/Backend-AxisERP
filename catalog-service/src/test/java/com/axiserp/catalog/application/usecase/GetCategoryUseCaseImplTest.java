package com.axiserp.catalog.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.domain.exception.CategoryNotFoundException;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
class GetCategoryUseCaseImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private GetCategoryUseCaseImpl getCategoryUseCase;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should get category by id successfully")
    void getById_success() {
        Category category = Category.builder()
                .id(categoryId)
                .name("Test Category")
                .description("Test description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponse response = getCategoryUseCase.getById(categoryId);

        assertNotNull(response);
        assertEquals("Test Category", response.getName());
        assertEquals("Test description", response.getDescription());
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when not found")
    void getById_notFound() {
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> getCategoryUseCase.getById(categoryId));
    }
}
