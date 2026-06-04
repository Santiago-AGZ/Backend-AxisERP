package com.axiserp.inventory.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.inventory.application.dto.response.ProductInventoryResponse;
import com.axiserp.inventory.application.dto.response.ProductSummary;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.PageResult;
import com.axiserp.inventory.ports.input.GetDepletedAlertsUseCase;
import com.axiserp.inventory.ports.output.CatalogServicePort;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDepletedAlertsUseCaseImpl implements GetDepletedAlertsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetDepletedAlertsUseCaseImpl.class);

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final CatalogServicePort catalogServicePort;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProductInventoryResponse> execute(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            size = Math.min(Math.max(size, 1), 100);
        }
        List<Inventory> inventories = inventoryRepositoryPort.findDepleted(page, size);
        long total = inventoryRepositoryPort.countDepleted();

        List<UUID> productIds = inventories.stream()
                .map(Inventory::getProductId)
                .toList();
        Map<UUID, ProductSummary> productSummaries = catalogServicePort.findProductSummaries(productIds);

        List<ProductInventoryResponse> responses = inventories.stream()
                .map(inv -> toResponse(inv, productSummaries.get(inv.getProductId())))
                .toList();

        log.debug("depleted_alerts page={} size={} total={}", page, size, total);
        return new PageResult<>(responses, total);
    }

    private ProductInventoryResponse toResponse(Inventory inventory, ProductSummary summary) {
        return ProductInventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .productName(summary != null ? summary.getName() : null)
                .productCodigo(summary != null ? summary.getCodigo() : null)
                .currentStock(inventory.getCurrentStock())
                .minStock(inventory.getMinStock())
                .maxStock(inventory.getMaxStock() != null ? inventory.getMaxStock() : 0)
                .lowStock(inventory.isLowStock())
                .depleted(inventory.isDepleted())
                .lastMovementAt(inventory.getLastMovementAt())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}