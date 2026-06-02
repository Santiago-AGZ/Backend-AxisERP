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

import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.domain.exception.ProductNotFoundException;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

@ExtendWith(MockitoExtension.class)
class GetProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private GetProductUseCaseImpl getProductUseCase;

    private UUID productId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should get product by id successfully")
    void getById_success() {
        Product product = Product.builder()
                .id(productId)
                .name("Test Product")
                .codigo("TEST001")
                .categoryId(categoryId)
                .purchasePrice(new BigDecimal("10.00"))
                .salePrice(new BigDecimal("20.00"))
                .status(Product.ProductStatus.ACTIVO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Category category = Category.builder().id(categoryId).name("Test Category").build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));

        ProductResponse response = getProductUseCase.getById(productId);

        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        assertEquals("TEST001", response.getCodigo());
        assertEquals("Test Category", response.getCategory().getName());
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when not found")
    void getById_notFound() {
        when(productRepositoryPort.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> getProductUseCase.getById(productId));
    }
}
