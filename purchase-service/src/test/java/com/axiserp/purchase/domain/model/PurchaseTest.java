package com.axiserp.purchase.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Purchase Domain Model")
class PurchaseTest {

    @Test
    @DisplayName("Should be modifiable when status is BORRADOR")
    void isModifiable_returnsTrue_whenBorrador() {
        Purchase purchase = Purchase.builder()
                .status(PurchaseStatus.BORRADOR)
                .build();

        assertTrue(purchase.isModifiable());
    }

    @Test
    @DisplayName("Should be modifiable when status is PENDIENTE")
    void isModifiable_returnsTrue_whenPendiente() {
        Purchase purchase = Purchase.builder()
                .status(PurchaseStatus.PENDIENTE)
                .build();

        assertTrue(purchase.isModifiable());
    }

    @Test
    @DisplayName("Should be modifiable when status is RECIBIDA")
    void isModifiable_returnsTrue_whenRecibida() {
        Purchase purchase = Purchase.builder()
                .status(PurchaseStatus.RECIBIDA)
                .build();

        assertTrue(purchase.isModifiable());
    }

    @Test
    @DisplayName("Should not be modifiable when status is CANCELADA")
    void isModifiable_returnsFalse_whenCancelada() {
        Purchase purchase = Purchase.builder()
                .status(PurchaseStatus.CANCELADA)
                .build();

        assertFalse(purchase.isModifiable());
    }

    @Test
    @DisplayName("Should not be modifiable when status is PAGADA")
    void isModifiable_returnsFalse_whenPagada() {
        Purchase purchase = Purchase.builder()
                .status(PurchaseStatus.PAGADA)
                .build();

        assertFalse(purchase.isModifiable());
    }

    @Test
    @DisplayName("Should build purchase with items")
    void build_withItems() {
        PurchaseItem item = PurchaseItem.builder()
                .productId(java.util.UUID.randomUUID())
                .productName("Test Product")
                .quantity(5)
                .build();

        Purchase purchase = Purchase.builder()
                .status(PurchaseStatus.BORRADOR)
                .items(List.of(item))
                .build();

        assertEquals(1, purchase.getItems().size());
        assertEquals("Test Product", purchase.getItems().get(0).getProductName());
    }
}
