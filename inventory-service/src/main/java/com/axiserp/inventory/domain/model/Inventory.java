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
    private int maxStock;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
