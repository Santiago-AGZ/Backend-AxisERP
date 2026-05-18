package com.axiserp.catalog.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.catalog.application.dto.request.CreateCategoryRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.domain.exception.DuplicateCategoryException;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Category.CategoryStatus;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CreateCategoryUseCaseImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private CreateCategoryUseCaseImpl createCategoryUseCase;

    @Test
    @DisplayName("Should create category successfully")
    void create_success() {
        CreateCategoryRequest request = new CreateCategoryRequest("Electronics", "Electronic devices");
        Category saved = Category.builder()
                .id(UUID.randomUUID())
                .name("Electronics")
                .description("Electronic devices")
                .status(CategoryStatus.ACTIVA)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepositoryPort.existsByName("Electronics")).thenReturn(false);
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = createCategoryUseCase.create(request);

        assertNotNull(response);
        assertEquals("Electronics", response.getName());
        assertEquals("Electronic devices", response.getDescription());
        assertEquals("ACTIVA", response.getStatus());
        verify(categoryRepositoryPort).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw DuplicateCategoryException when name exists")
    void create_duplicateName() {
        CreateCategoryRequest request = new CreateCategoryRequest("Existing", null);

        when(categoryRepositoryPort.existsByName("Existing")).thenReturn(true);

        assertThrows(DuplicateCategoryException.class, () -> createCategoryUseCase.create(request));
        verify(categoryRepositoryPort, never()).save(any());
    }
}
