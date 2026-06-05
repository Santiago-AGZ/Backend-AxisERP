package com.axiserp.purchase.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.purchase.application.dto.request.ReceiveItemRequest;
import com.axiserp.purchase.application.dto.request.ReceivePurchaseRequest;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.exception.PurchaseNotModifiableException;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.domain.model.PurchaseItem;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.ports.output.InventoryServicePort;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceivePurchaseUseCaseImpl")
class ReceivePurchaseUseCaseImplTest {

    @Mock
    private PurchaseRepositoryPort purchaseRepositoryPort;
    @Mock
    private InventoryServicePort inventoryServicePort;

    private ReceivePurchaseUseCaseImpl receivePurchaseUseCase;

    private UUID purchaseId;
    private UUID itemId;
    private Purchase pendingPurchase;
    private PurchaseItem purchaseItem;

    @BeforeEach
    void setUp() {
        receivePurchaseUseCase = new ReceivePurchaseUseCaseImpl(purchaseRepositoryPort, inventoryServicePort);

        purchaseId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        purchaseItem = PurchaseItem.builder()
                .id(itemId)
                .productId(UUID.randomUUID())
                .productName("Producto Test")
                .quantity(10)
                .receivedQuantity(0)
                .unitPrice(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("1000.00"))
                .build();

        pendingPurchase = Purchase.builder()
                .id(purchaseId)
                .supplierId(UUID.randomUUID())
                .status(PurchaseStatus.PENDIENTE)
                .items(List.of(purchaseItem))
                .build();
    }

    @Test
    @DisplayName("Should receive purchase partially")
    void execute_partialReception_success() {
        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(pendingPurchase));
        when(purchaseRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceivePurchaseRequest request = ReceivePurchaseRequest.builder()
                .items(List.of(
                        ReceiveItemRequest.builder()
                                .itemId(itemId)
                                .receivedQuantity(3)
                                .build()))
                .build();

        PurchaseResponse response = receivePurchaseUseCase.execute(purchaseId, request);

        assertEquals(PurchaseStatus.PENDIENTE, response.getStatus());
        assertEquals(3, response.getItems().get(0).getReceivedQuantity());
        verify(inventoryServicePort).registerEntry(
                purchaseItem.getProductId(), 3, "COMPRA", purchaseId, null);
    }

    @Test
    @DisplayName("Should set status to RECIBIDA when all items received")
    void execute_fullReception_setsRecibida() {
        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(pendingPurchase));
        when(purchaseRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceivePurchaseRequest request = ReceivePurchaseRequest.builder()
                .items(List.of(
                        ReceiveItemRequest.builder()
                                .itemId(itemId)
                                .receivedQuantity(10)
                                .build()))
                .build();

        PurchaseResponse response = receivePurchaseUseCase.execute(purchaseId, request);

        assertEquals(PurchaseStatus.RECIBIDA, response.getStatus());
        assertEquals(10, response.getItems().get(0).getReceivedQuantity());
        verify(inventoryServicePort).registerEntry(
                purchaseItem.getProductId(), 10, "COMPRA", purchaseId, null);
    }

    @Test
    @DisplayName("Should throw PurchaseNotFoundException when purchase not found")
    void execute_purchaseNotFound_throws() {
        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.empty());

        ReceivePurchaseRequest request = ReceivePurchaseRequest.builder()
                .items(List.of())
                .build();

        assertThrows(PurchaseNotFoundException.class,
                () -> receivePurchaseUseCase.execute(purchaseId, request));
    }

    @Test
    @DisplayName("Should throw PurchaseNotModifiableException when not PENDIENTE")
    void execute_notPendiente_throws() {
        Purchase cancelledPurchase = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.CANCELADA)
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(cancelledPurchase));

        ReceivePurchaseRequest request = ReceivePurchaseRequest.builder()
                .items(List.of())
                .build();

        assertThrows(PurchaseNotModifiableException.class,
                () -> receivePurchaseUseCase.execute(purchaseId, request));
    }

    @Test
    @DisplayName("Should throw when item not found in purchase")
    void execute_itemNotFound_throws() {
        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(pendingPurchase));

        ReceivePurchaseRequest request = ReceivePurchaseRequest.builder()
                .items(List.of(
                        ReceiveItemRequest.builder()
                                .itemId(UUID.randomUUID())
                                .receivedQuantity(5)
                                .build()))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> receivePurchaseUseCase.execute(purchaseId, request));
    }

    @Test
    @DisplayName("Should throw when received quantity exceeds ordered quantity")
    void execute_quantityExceeds_throws() {
        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(pendingPurchase));

        ReceivePurchaseRequest request = ReceivePurchaseRequest.builder()
                .items(List.of(
                        ReceiveItemRequest.builder()
                                .itemId(itemId)
                                .receivedQuantity(20)
                                .build()))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> receivePurchaseUseCase.execute(purchaseId, request));
    }
}
