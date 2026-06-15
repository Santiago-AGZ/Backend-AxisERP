package com.axiserp.inventory.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.inventory.application.dto.request.AdjustmentRequest;
import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.application.service.AuditService;
import com.axiserp.inventory.domain.exception.InsufficientStockException;
import com.axiserp.inventory.domain.exception.InventoryNotFoundException;
import com.axiserp.inventory.domain.exception.NegativeQuantityException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterAdjustmentUseCaseImpl")
class RegisterAdjustmentUseCaseImplTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private AuditService auditService;

    private RegisterAdjustmentUseCaseImpl registerAdjustmentUseCase;
    private UUID productId;
    private UUID createdBy;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        registerAdjustmentUseCase = new RegisterAdjustmentUseCaseImpl(inventoryRepositoryPort, auditService);
        productId = UUID.randomUUID();
        createdBy = UUID.randomUUID();
        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(50)
                .build();
    }

    @Test
    @DisplayName("[R13] Should reject adjustment with null justification")
    void registerAdjustment_nullJustification_throws() {
        AdjustmentRequest request = AdjustmentRequest.builder()
                .adjustmentType(AdjustmentRequest.AdjustmentType.POSITIVO)
                .quantity(10)
                .justification(null)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registerAdjustmentUseCase.registerAdjustment(productId, request, createdBy));
        assertTrue(ex.getMessage().contains("justificacion") || ex.getMessage().contains("obligatoria"));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R13] Should reject adjustment with blank justification")
    void registerAdjustment_blankJustification_throws() {
        AdjustmentRequest request = AdjustmentRequest.builder()
                .adjustmentType(AdjustmentRequest.AdjustmentType.POSITIVO)
                .quantity(10)
                .justification("   ")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registerAdjustmentUseCase.registerAdjustment(productId, request, createdBy));
        assertTrue(ex.getMessage().contains("justificacion") || ex.getMessage().contains("obligatoria"));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R13] Should accept positive adjustment with justification")
    void registerAdjustment_positiveWithJustification_success() {
        AdjustmentRequest request = AdjustmentRequest.builder()
                .adjustmentType(AdjustmentRequest.AdjustmentType.POSITIVO)
                .quantity(10)
                .justification("Inventory count correction")
                .build();

        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryRepositoryPort.saveMovement(any())).thenAnswer(inv -> inv.getArgument(0));

        MovementResponse response = registerAdjustmentUseCase.registerAdjustment(productId, request, createdBy);

        assertNotNull(response);
        assertEquals(MovementType.AJUSTE_POSITIVO.name(), response.getMovementType());
        assertEquals(60, response.getNewStock());
        assertEquals(50, response.getPreviousStock());
        assertEquals("Inventory count correction", response.getJustification());
        verify(inventoryRepositoryPort).save(any(Inventory.class));
    }

    @Test
    @DisplayName("[R13] Should accept negative adjustment with justification")
    void registerAdjustment_negativeWithJustification_success() {
        AdjustmentRequest request = AdjustmentRequest.builder()
                .adjustmentType(AdjustmentRequest.AdjustmentType.NEGATIVO)
                .quantity(10)
                .justification("Damaged goods write-off")
                .build();

        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryRepositoryPort.saveMovement(any())).thenAnswer(inv -> inv.getArgument(0));

        MovementResponse response = registerAdjustmentUseCase.registerAdjustment(productId, request, createdBy);

        assertNotNull(response);
        assertEquals(MovementType.AJUSTE_NEGATIVO.name(), response.getMovementType());
        assertEquals(40, response.getNewStock());
        assertEquals(50, response.getPreviousStock());
        assertEquals("Damaged goods write-off", response.getJustification());
        verify(inventoryRepositoryPort).save(any(Inventory.class));
    }

    @Test
    @DisplayName("[R13] Should reject negative adjustment when stock insufficient")
    void registerAdjustment_negativeInsufficientStock_throws() {
        AdjustmentRequest request = AdjustmentRequest.builder()
                .adjustmentType(AdjustmentRequest.AdjustmentType.NEGATIVO)
                .quantity(100)
                .justification("Attempting to remove too much")
                .build();

        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class,
                () -> registerAdjustmentUseCase.registerAdjustment(productId, request, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R13] Should reject adjustment with zero quantity")
    void registerAdjustment_zeroQuantity_throws() {
        AdjustmentRequest request = AdjustmentRequest.builder()
                .adjustmentType(AdjustmentRequest.AdjustmentType.POSITIVO)
                .quantity(0)
                .justification("Test justification")
                .build();

        assertThrows(NegativeQuantityException.class,
                () -> registerAdjustmentUseCase.registerAdjustment(productId, request, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R13] Should throw InventoryNotFoundException when product has no inventory")
    void registerAdjustment_productNotFound_throws() {
        AdjustmentRequest request = AdjustmentRequest.builder()
                .adjustmentType(AdjustmentRequest.AdjustmentType.POSITIVO)
                .quantity(5)
                .justification("Test justification")
                .build();

        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> registerAdjustmentUseCase.registerAdjustment(productId, request, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }
}
