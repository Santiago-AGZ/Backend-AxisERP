package com.axiserp.inventory.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.inventory.application.dto.response.InventoryResponse;
import com.axiserp.inventory.domain.exception.InventoryNotFoundException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.ports.input.GetInventoryUseCase;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetInventoryUseCaseImpl implements GetInventoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetInventoryUseCaseImpl.class);

    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(UUID productId) {
        Inventory inventory = inventoryRepositoryPort.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        log.debug("inventory_get productId={} currentStock={}", productId, inventory.getCurrentStock());

        return toResponse(inventory);
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .currentStock(inventory.getCurrentStock())
                .minStock(inventory.getMinStock())
                .maxStock(inventory.getMaxStock())
                .lowStock(inventory.isLowStock())
                .depleted(inventory.isDepleted())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
