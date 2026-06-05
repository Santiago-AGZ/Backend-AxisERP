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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.application.service.AuditService;
import com.axiserp.inventory.domain.exception.InsufficientStockException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReverseMovementUseCaseImpl")
class ReverseMovementUseCaseImplTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ReverseMovementUseCaseImpl reverseMovementUseCase;

    private UUID movementId;
    private UUID productId;
    private UUID createdBy;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        movementId = UUID.randomUUID();
        productId = UUID.randomUUID();
        createdBy = UUID.randomUUID();
        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(50)
                .build();
    }

    @Test
    @DisplayName("Should reverse an ENTRADA movement by subtracting stock")
    void reverse_entrada_subtractsStock() {
        InventoryMovement originalMovement = InventoryMovement.builder()
                .id(movementId)
                .productId(productId)
                .movementType(MovementType.ENTRADA)
                .quantity(10)
                .build();

        when(inventoryRepositoryPort.findMovementById(movementId)).thenReturn(Optional.of(originalMovement));
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryRepositoryPort.saveMovement(any(InventoryMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MovementResponse response = reverseMovementUseCase.reverse(movementId, "Error en entrada", createdBy);

        assertNotNull(response);
        assertEquals(MovementType.ANULACION.name(), response.getMovementType());
        assertEquals(40, response.getNewStock());
        assertEquals(50, response.getPreviousStock());
        assertEquals("Error en entrada", response.getJustification());
        verify(inventoryRepositoryPort).save(any(Inventory.class));
        verify(inventoryRepositoryPort).saveMovement(any(InventoryMovement.class));
        verify(auditService).logReversal(productId, createdBy, movementId, 10);
    }

    @Test
    @DisplayName("Should reverse a SALIDA movement by adding stock back")
    void reverse_salida_addsStockBack() {
        InventoryMovement originalMovement = InventoryMovement.builder()
                .id(movementId)
                .productId(productId)
                .movementType(MovementType.SALIDA)
                .quantity(10)
                .build();

        when(inventoryRepositoryPort.findMovementById(movementId)).thenReturn(Optional.of(originalMovement));
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryRepositoryPort.saveMovement(any(InventoryMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MovementResponse response = reverseMovementUseCase.reverse(movementId, "Error en salida", createdBy);

        assertNotNull(response);
        assertEquals(60, response.getNewStock());
        assertEquals(50, response.getPreviousStock());
    }

    @Test
    @DisplayName("Should throw when justification is blank")
    void reverse_emptyJustification_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> reverseMovementUseCase.reverse(movementId, "", createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when justification is null")
    void reverse_nullJustification_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> reverseMovementUseCase.reverse(movementId, null, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when reversal causes negative stock")
    void reverse_reversalCausesNegativeStock_throws() {
        InventoryMovement originalMovement = InventoryMovement.builder()
                .id(movementId)
                .productId(productId)
                .movementType(MovementType.ENTRADA)
                .quantity(100)
                .build();
        Inventory lowInventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(30)
                .build();

        when(inventoryRepositoryPort.findMovementById(movementId)).thenReturn(Optional.of(originalMovement));
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(lowInventory));

        assertThrows(InsufficientStockException.class,
                () -> reverseMovementUseCase.reverse(movementId, "Intento de reversal con stock insuficiente", createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InventoryNotFoundException when movement is missing")
    void reverse_movementNotFound_throws() {
        when(inventoryRepositoryPort.findMovementById(movementId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reverseMovementUseCase.reverse(movementId, "Justificacion", createdBy));
    }
}
