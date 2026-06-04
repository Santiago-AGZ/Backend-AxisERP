package com.axiserp.inventory.application.dto.response;

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
public class ProductInventoryResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private String productCodigo;
    private int currentStock;
    private int minStock;
    private int maxStock;
    private boolean lowStock;
    private boolean depleted;
    private LocalDateTime lastMovementAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}