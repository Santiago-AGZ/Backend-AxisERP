package com.axiserp.inventory.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.inventory.application.dto.request.InitializeInventoryRequest;
import com.axiserp.inventory.application.dto.response.InventoryResponse;
import com.axiserp.inventory.application.dto.response.ProductSummary;
import com.axiserp.inventory.application.service.AuditService;
import com.axiserp.inventory.domain.exception.InventoryAlreadyInitializedException;
import com.axiserp.inventory.domain.exception.InvalidStockConfigException;
import com.axiserp.inventory.domain.exception.ProductNotActiveException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.ports.output.CatalogServicePort;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("InitializeInventoryUseCaseImpl")
class InitializeInventoryUseCaseImplTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private CatalogServicePort catalogServicePort;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InitializeInventoryUseCaseImpl initializeInventoryUseCase;

    private UUID productId;
    private UUID createdBy;
    private InitializeInventoryRequest request;
    private ProductSummary activeProduct;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        createdBy = UUID.randomUUID();
        request = InitializeInventoryRequest.builder()
                .productId(productId)
                .initialStock(100)
                .minStock(10)
                .maxStock(200)
                .build();
        activeProduct = ProductSummary.builder()
                .id(productId)
                .name("Test Product")
                .status("ACTIVO")
                .build();
    }

    @Test
    @DisplayName("Should initialize inventory with stock and create movement")
    void initialize_withStock_success() {
        when(catalogServicePort.findProductSummaries(anyList()))
                .thenReturn(Map.of(productId, activeProduct));
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.empty());
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryRepositoryPort.saveMovement(any(InventoryMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        InventoryResponse response = initializeInventoryUseCase.initialize(request, createdBy);

        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals(100, response.getCurrentStock());
        assertEquals(10, response.getMinStock());
        assertEquals(200, response.getMaxStock());
        verify(inventoryRepositoryPort).save(any(Inventory.class));
        verify(inventoryRepositoryPort).saveMovement(any(InventoryMovement.class));
        verify(auditService).logInitialize(productId, createdBy, 100);
    }

    @Test
    @DisplayName("Should initialize inventory with zero stock without creating movement")
    void initialize_zeroStock_createsNoMovement() {
        InitializeInventoryRequest zeroStockRequest = InitializeInventoryRequest.builder()
                .productId(productId)
                .initialStock(0)
                .minStock(5)
                .maxStock(50)
                .build();

        when(catalogServicePort.findProductSummaries(anyList()))
                .thenReturn(Map.of(productId, activeProduct));
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.empty());
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryResponse response = initializeInventoryUseCase.initialize(zeroStockRequest, createdBy);

        assertNotNull(response);
        assertEquals(0, response.getCurrentStock());
        verify(inventoryRepositoryPort).save(any(Inventory.class));
        verify(inventoryRepositoryPort, never()).saveMovement(any());
        verify(auditService).logInitialize(productId, createdBy, 0);
    }

    @Test
    @DisplayName("Should throw InventoryAlreadyInitializedException when already exists")
    void initialize_alreadyExists_throws() {
        when(catalogServicePort.findProductSummaries(anyList()))
                .thenReturn(Map.of(productId, activeProduct));
        when(inventoryRepositoryPort.findByProductId(productId))
                .thenReturn(Optional.of(Inventory.builder().build()));

        assertThrows(InventoryAlreadyInitializedException.class,
                () -> initializeInventoryUseCase.initialize(request, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidStockConfigException when maxStock <= minStock")
    void initialize_invalidStockConfig_throws() {
        InitializeInventoryRequest invalidRequest = InitializeInventoryRequest.builder()
                .productId(productId)
                .initialStock(100)
                .minStock(50)
                .maxStock(30)
                .build();

        when(catalogServicePort.findProductSummaries(anyList()))
                .thenReturn(Map.of(productId, activeProduct));
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(InvalidStockConfigException.class,
                () -> initializeInventoryUseCase.initialize(invalidRequest, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ProductNotActiveException when product is not active")
    void initialize_productNotActive_throws() {
        ProductSummary inactiveProduct = ProductSummary.builder()
                .id(productId)
                .name("Inactive Product")
                .status("INACTIVO")
                .build();

        when(catalogServicePort.findProductSummaries(anyList()))
                .thenReturn(Map.of(productId, inactiveProduct));

        assertThrows(ProductNotActiveException.class,
                () -> initializeInventoryUseCase.initialize(request, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ProductNotActiveException when product not found")
    void initialize_productNotFound_throws() {
        Map<UUID, ProductSummary> emptyMap = new HashMap<>();
        emptyMap.put(productId, null);

        when(catalogServicePort.findProductSummaries(anyList()))
                .thenReturn(emptyMap);

        assertThrows(ProductNotActiveException.class,
                () -> initializeInventoryUseCase.initialize(request, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should accept inventory without maxStock configured")
    void initialize_noMaxStock_success() {
        InitializeInventoryRequest noMaxRequest = InitializeInventoryRequest.builder()
                .productId(productId)
                .initialStock(50)
                .minStock(5)
                .maxStock(0)
                .build();

        when(catalogServicePort.findProductSummaries(anyList()))
                .thenReturn(Map.of(productId, activeProduct));
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.empty());
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryRepositoryPort.saveMovement(any(InventoryMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        InventoryResponse response = initializeInventoryUseCase.initialize(noMaxRequest, createdBy);

        assertNotNull(response);
        assertEquals(0, response.getMaxStock());
    }
}
