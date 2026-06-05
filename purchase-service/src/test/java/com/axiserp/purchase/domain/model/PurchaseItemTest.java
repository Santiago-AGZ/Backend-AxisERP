package com.axiserp.purchase.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PurchaseItem Domain Model")
class PurchaseItemTest {

    @Test
    @DisplayName("pendingQuantity should return quantity minus receivedQuantity")
    void pendingQuantity_returnsDifference() {
        PurchaseItem item = PurchaseItem.builder()
                .quantity(10)
                .receivedQuantity(3)
                .build();

        assertEquals(7, item.pendingQuantity());
    }

    @Test
    @DisplayName("pendingQuantity should return full quantity when nothing received")
    void pendingQuantity_returnsFull_whenNoneReceived() {
        PurchaseItem item = PurchaseItem.builder()
                .quantity(10)
                .receivedQuantity(0)
                .build();

        assertEquals(10, item.pendingQuantity());
    }

    @Test
    @DisplayName("pendingQuantity should return zero when fully received")
    void pendingQuantity_returnsZero_whenFullyReceived() {
        PurchaseItem item = PurchaseItem.builder()
                .quantity(10)
                .receivedQuantity(10)
                .build();

        assertEquals(0, item.pendingQuantity());
    }

    @Test
    @DisplayName("isFullyReceived should return true when receivedQuantity equals quantity")
    void isFullyReceived_returnsTrue_whenEqual() {
        PurchaseItem item = PurchaseItem.builder()
                .quantity(10)
                .receivedQuantity(10)
                .build();

        assertTrue(item.isFullyReceived());
    }

    @Test
    @DisplayName("isFullyReceived should return true when receivedQuantity exceeds quantity")
    void isFullyReceived_returnsTrue_whenExceeds() {
        PurchaseItem item = PurchaseItem.builder()
                .quantity(10)
                .receivedQuantity(15)
                .build();

        assertTrue(item.isFullyReceived());
    }

    @Test
    @DisplayName("isFullyReceived should return false when receivedQuantity is less")
    void isFullyReceived_returnsFalse_whenLess() {
        PurchaseItem item = PurchaseItem.builder()
                .quantity(10)
                .receivedQuantity(7)
                .build();

        assertFalse(item.isFullyReceived());
    }

    @Test
    @DisplayName("isFullyReceived should return false when nothing received")
    void isFullyReceived_returnsFalse_whenNone() {
        PurchaseItem item = PurchaseItem.builder()
                .quantity(10)
                .receivedQuantity(0)
                .build();

        assertFalse(item.isFullyReceived());
    }

    @Test
    @DisplayName("Should build with subtotal")
    void build_withSubtotal() {
        PurchaseItem item = PurchaseItem.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Test Product")
                .quantity(5)
                .receivedQuantity(0)
                .unitPrice(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("500.00"))
                .build();

        assertEquals("Test Product", item.getProductName());
        assertEquals(5, item.getQuantity());
        assertEquals(0, item.getReceivedQuantity());
        assertEquals(new BigDecimal("100.00"), item.getUnitPrice());
        assertEquals(new BigDecimal("500.00"), item.getSubtotal());
    }
}
