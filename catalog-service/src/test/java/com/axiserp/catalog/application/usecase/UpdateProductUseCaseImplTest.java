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

import com.axiserp.catalog.application.dto.request.UpdateProductRequest;
import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.domain.exception.InvalidPriceException;
import com.axiserp.catalog.domain.exception.ProductNotFoundException;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private UpdateProductUseCaseImpl updateProductUseCase;

    private UUID productId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should update product successfully")
    void update_success() {
        Product existing = Product.builder()
                .id(productId)
                .name("Old Name")
                .codigo("TEST001")
                .categoryId(categoryId)
                .purchasePrice(new BigDecimal("10.00"))
                .salePrice(new BigDecimal("20.00"))
                .status(Product.ProductStatus.ACTIVO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Category category = Category.builder().id(categoryId).name("Test Category").build();
        Product saved = Product.builder()
                .id(productId)
                .name("New Name")
                .codigo("TEST001")
                .categoryId(categoryId)
                .purchasePrice(new BigDecimal("10.00"))
                .salePrice(new BigDecimal("25.00"))
                .status(Product.ProductStatus.ACTIVO)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        UpdateProductRequest request = new UpdateProductRequest("New Name", null, null, new BigDecimal("25.00"));

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(existing));
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = updateProductUseCase.update(productId, request);

        assertNotNull(response);
        assertEquals("New Name", response.getName());
        assertEquals(new BigDecimal("25.00"), response.getSalePrice());
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when not found")
    void update_notFound() {
        when(productRepositoryPort.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> updateProductUseCase.update(productId, new UpdateProductRequest()));
    }

    @Test
    @DisplayName("Should throw InvalidPriceException when salePrice < purchasePrice")
    void update_invalidPrice() {
        Product existing = Product.builder()
                .id(productId)
                .name("Test")
                .codigo("TEST001")
                .categoryId(categoryId)
                .purchasePrice(new BigDecimal("20.00"))
                .salePrice(new BigDecimal("30.00"))
                .status(Product.ProductStatus.ACTIVO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(existing));

        UpdateProductRequest request = new UpdateProductRequest(null, null, null, new BigDecimal("10.00"));

        assertThrows(InvalidPriceException.class, () -> updateProductUseCase.update(productId, request));
    }
}
