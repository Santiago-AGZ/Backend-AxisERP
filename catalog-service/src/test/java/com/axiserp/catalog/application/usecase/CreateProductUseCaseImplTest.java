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

import com.axiserp.catalog.application.dto.request.CreateProductRequest;
import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.application.service.CatalogAuditService;
import com.axiserp.catalog.domain.exception.DuplicateCodigoException;
import com.axiserp.catalog.domain.exception.InvalidPriceException;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @Mock
    private CatalogAuditService catalogAuditService;

    @InjectMocks
    private CreateProductUseCaseImpl createProductUseCase;

    private UUID categoryId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        adminId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create product successfully")
    void create_success() {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product", "TEST001", null, categoryId,
                new BigDecimal("10.00"), new BigDecimal("20.00"));

        Category category = Category.builder().id(categoryId).name("Test Category").status(Category.CategoryStatus.ACTIVA).build();
        Product saved = Product.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .codigo("TEST001")
                .categoryId(categoryId)
                .purchasePrice(new BigDecimal("10.00"))
                .salePrice(new BigDecimal("20.00"))
                .status(Product.ProductStatus.ACTIVO)
                .createdBy(adminId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepositoryPort.existsByCodigo("TEST001")).thenReturn(false);
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = createProductUseCase.create(request, adminId);

        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        assertEquals("TEST001", response.getCodigo());
        assertEquals("ACTIVO", response.getStatus());
        verify(productRepositoryPort).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw DuplicateCodigoException when codigo exists")
    void create_duplicateCodigo() {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product", "EXISTING", null, categoryId,
                new BigDecimal("10.00"), new BigDecimal("20.00"));

        when(productRepositoryPort.existsByCodigo("EXISTING")).thenReturn(true);

        assertThrows(DuplicateCodigoException.class, () -> createProductUseCase.create(request, adminId));
        verify(productRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidPriceException when salePrice < purchasePrice")
    void create_invalidPrice() {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product", "TEST002", null, categoryId,
                new BigDecimal("20.00"), new BigDecimal("10.00"));

        when(productRepositoryPort.existsByCodigo("TEST002")).thenReturn(false);

        assertThrows(InvalidPriceException.class, () -> createProductUseCase.create(request, adminId));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when category not found")
    void create_invalidCategory() {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product", "TEST003", null, categoryId,
                new BigDecimal("10.00"), new BigDecimal("20.00"));

        when(productRepositoryPort.existsByCodigo("TEST003")).thenReturn(false);
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> createProductUseCase.create(request, adminId));
    }

    @Test
    @DisplayName("[R4] Should throw DuplicateCodigoException when codigo belongs to deactivated product")
    void create_duplicateCodigo_deactivatedProduct() {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product", "DELETED-CODE", null, categoryId,
                new BigDecimal("10.00"), new BigDecimal("20.00"));

        when(productRepositoryPort.existsByCodigo("DELETED-CODE")).thenReturn(true);

        assertThrows(DuplicateCodigoException.class, () -> createProductUseCase.create(request, adminId));
        verify(productRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R7] Should throw IllegalStateException when category is inactive")
    void create_inactiveCategory_throws() {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product", "TEST004", null, categoryId,
                new BigDecimal("10.00"), new BigDecimal("20.00"));

        Category inactiveCategory = Category.builder()
                .id(categoryId)
                .name("Inactive Category")
                .status(Category.CategoryStatus.INACTIVA)
                .build();

        when(productRepositoryPort.existsByCodigo("TEST004")).thenReturn(false);
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(inactiveCategory));

        assertThrows(IllegalStateException.class, () -> createProductUseCase.create(request, adminId));
        verify(productRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R7] Should throw IllegalStateException when category is deleted")
    void create_deletedCategory_throws() {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product", "TEST005", null, categoryId,
                new BigDecimal("10.00"), new BigDecimal("20.00"));

        Category deletedCategory = Category.builder()
                .id(categoryId)
                .name("Deleted Category")
                .status(Category.CategoryStatus.ELIMINADA)
                .build();

        when(productRepositoryPort.existsByCodigo("TEST005")).thenReturn(false);
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(deletedCategory));

        assertThrows(IllegalStateException.class, () -> createProductUseCase.create(request, adminId));
        verify(productRepositoryPort, never()).save(any());
    }
}
