package com.axiserp.inventory.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.axiserp.inventory.domain.exception.InsufficientStockException;

@DisplayName("Inventory Domain Model")
class InventoryTest {

    @Test
    @DisplayName("Should add stock to inventory")
    void addStock_increasesCurrentStock() {
        Inventory inventory = Inventory.builder().currentStock(10).build();

        inventory.addStock(5);

        assertEquals(15, inventory.getCurrentStock());
    }

    @Test
    @DisplayName("Should throw when adding zero stock")
    void addStock_zero_throws() {
        Inventory inventory = Inventory.builder().currentStock(10).build();

        assertThrows(IllegalArgumentException.class, () -> inventory.addStock(0));
        assertEquals(10, inventory.getCurrentStock());
    }

    @Test
    @DisplayName("Should throw when adding negative stock")
    void addStock_negative_throws() {
        Inventory inventory = Inventory.builder().currentStock(10).build();

        assertThrows(IllegalArgumentException.class, () -> inventory.addStock(-5));
        assertEquals(10, inventory.getCurrentStock());
    }

    @Test
    @DisplayName("Should subtract stock from inventory")
    void subtractStock_decreasesCurrentStock() {
        Inventory inventory = Inventory.builder().currentStock(10).build();

        inventory.subtractStock(3);

        assertEquals(7, inventory.getCurrentStock());
    }

    @Test
    @DisplayName("Should throw when subtracting zero stock")
    void subtractStock_zero_throws() {
        Inventory inventory = Inventory.builder().currentStock(10).build();

        assertThrows(IllegalArgumentException.class, () -> inventory.subtractStock(0));
        assertEquals(10, inventory.getCurrentStock());
    }

    @Test
    @DisplayName("Should throw when subtracting negative stock")
    void subtractStock_negative_throws() {
        Inventory inventory = Inventory.builder().currentStock(10).build();

        assertThrows(IllegalArgumentException.class, () -> inventory.subtractStock(-1));
        assertEquals(10, inventory.getCurrentStock());
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when not enough stock")
    void subtractStock_insufficient_throws() {
        Inventory inventory = Inventory.builder().currentStock(5).build();

        assertThrows(InsufficientStockException.class, () -> inventory.subtractStock(10));
        assertEquals(5, inventory.getCurrentStock());
    }

    @Test
    @DisplayName("canExit should return true when stock is sufficient")
    void canExit_returnsTrue_whenStockSufficient() {
        Inventory inventory = Inventory.builder().currentStock(10).build();

        assertTrue(inventory.canExit(5));
        assertTrue(inventory.canExit(10));
    }

    @Test
    @DisplayName("canExit should return false when stock is insufficient")
    void canExit_returnsFalse_whenStockInsufficient() {
        Inventory inventory = Inventory.builder().currentStock(10).build();

        assertFalse(inventory.canExit(11));
        assertFalse(inventory.canExit(100));
    }

    @Test
    @DisplayName("isLowStock should return true when stock is at or below minimum")
    void isLowStock_returnsTrue_whenAtOrBelowMin() {
        Inventory inventory = Inventory.builder().currentStock(5).minStock(10).build();

        assertTrue(inventory.isLowStock());
    }

    @Test
    @DisplayName("isLowStock should return false when stock is above minimum")
    void isLowStock_returnsFalse_whenAboveMin() {
        Inventory inventory = Inventory.builder().currentStock(15).minStock(10).build();

        assertFalse(inventory.isLowStock());
    }

    @Test
    @DisplayName("isLowStock should return false when stock is zero")
    void isLowStock_returnsFalse_whenDepleted() {
        Inventory inventory = Inventory.builder().currentStock(0).minStock(10).build();

        assertFalse(inventory.isLowStock());
    }

    @Test
    @DisplayName("isDepleted should return true when stock is zero")
    void isDepleted_returnsTrue_whenZero() {
        Inventory inventory = Inventory.builder().currentStock(0).build();

        assertTrue(inventory.isDepleted());
    }

    @Test
    @DisplayName("isDepleted should return false when stock is positive")
    void isDepleted_returnsFalse_whenPositive() {
        Inventory inventory = Inventory.builder().currentStock(10).build();

        assertFalse(inventory.isDepleted());
    }
}
