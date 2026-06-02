package com.axiserp.inventory.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryResponse {

    private UUID id;
    private UUID productId;
    private int currentStock;
    private int minStock;
    private int maxStock;
    private boolean lowStock;
    private boolean depleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
