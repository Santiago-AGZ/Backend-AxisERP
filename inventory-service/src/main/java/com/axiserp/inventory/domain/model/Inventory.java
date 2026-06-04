package com.axiserp.inventory.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private UUID id;
    private UUID productId;
    private int currentStock;
    private int minStock;
    private Integer maxStock;
    private int reservedStock;
    private Long version;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime lastMovementAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.currentStock += quantity;
    }

    public void subtractStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (!canExit(quantity)) {
            throw new com.axiserp.inventory.domain.exception.InsufficientStockException(quantity, this.currentStock);
        }
        this.currentStock -= quantity;
    }

    public boolean isLowStock() {
        return currentStock > 0 && currentStock <= minStock;
    }

    public boolean isDepleted() {
        return currentStock == 0;
    }

    public boolean canExit(int qty) {
        return currentStock >= qty;
    }
}
