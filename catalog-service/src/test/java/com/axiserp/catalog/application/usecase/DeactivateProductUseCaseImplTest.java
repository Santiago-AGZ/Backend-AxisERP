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
import com.axiserp.catalog.application.service.CatalogAuditService;
import com.axiserp.catalog.domain.exception.ProductNotFoundException;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;
import com.axiserp.catalog.ports.output.SalesServicePort;

@ExtendWith(MockitoExtension.class)
class DeactivateProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @Mock
    private SalesServicePort salesServicePort;

    @Mock
    private CatalogAuditService catalogAuditService;

    @InjectMocks
    private DeactivateProductUseCaseImpl deactivateProductUseCase;

    private UUID productId;
    private UUID categoryId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should deactivate product successfully")
    void deactivate_success() {
        Product existing = Product.builder()
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
        Product saved = Product.builder()
                .id(productId)
                .name("Test Product")
                .codigo("TEST001")
                .categoryId(categoryId)
                .purchasePrice(new BigDecimal("10.00"))
                .salePrice(new BigDecimal("20.00"))
                .status(Product.ProductStatus.ELIMINADO)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(existing));
        when(salesServicePort.hasPendingSales(productId)).thenReturn(false);
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = deactivateProductUseCase.deactivate(productId, userId);

        assertNotNull(response);
        assertEquals("ELIMINADO", response.getStatus());
        verify(productRepositoryPort).save(argThat(p -> p.getStatus() == Product.ProductStatus.ELIMINADO));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when not found")
    void deactivate_notFound() {
        when(productRepositoryPort.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> deactivateProductUseCase.deactivate(productId, userId));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when product has pending sales")
    void deactivate_hasPendingSales() {
        Product existing = Product.builder()
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

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(existing));
        when(salesServicePort.hasPendingSales(productId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> deactivateProductUseCase.deactivate(productId, userId));
        verify(productRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when already inactive")
    void deactivate_alreadyInactive() {
        Product existing = Product.builder()
                .id(productId)
                .name("Test Product")
                .codigo("TEST001")
                .categoryId(categoryId)
                .purchasePrice(new BigDecimal("10.00"))
                .salePrice(new BigDecimal("20.00"))
                .status(Product.ProductStatus.INACTIVO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> deactivateProductUseCase.deactivate(productId, userId));
    }
}
