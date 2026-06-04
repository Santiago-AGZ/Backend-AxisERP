package com.axiserp.inventory.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.inventory.application.dto.response.ProductInventoryResponse;
import com.axiserp.inventory.application.dto.response.ProductSummary;
import com.axiserp.inventory.domain.exception.InventoryNotFoundException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.PageResult;
import com.axiserp.inventory.ports.input.ListProductsUseCase;
import com.axiserp.inventory.ports.output.CatalogServicePort;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListProductsUseCaseImpl implements ListProductsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListProductsUseCaseImpl.class);

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final CatalogServicePort catalogServicePort;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProductInventoryResponse> list(int page, int size, UUID categoryId) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            size = Math.min(Math.max(size, 1), 100);
        }

        List<Inventory> inventories;
        long total;

        if (categoryId != null) {
            List<UUID> productIds = catalogServicePort.findProductIdsByCategoryId(categoryId);
            if (productIds.isEmpty()) {
                return new PageResult<>(List.of(), 0);
            }
            inventories = inventoryRepositoryPort.findByProductIds(productIds, page, size);
            total = inventoryRepositoryPort.countByProductIds(productIds);
        } else {
            inventories = inventoryRepositoryPort.findAll(page, size);
            total = inventoryRepositoryPort.countAll();
        }

        List<UUID> productIds = inventories.stream()
                .map(Inventory::getProductId)
                .toList();
        Map<UUID, ProductSummary> productSummaries = catalogServicePort.findProductSummaries(productIds);

        List<ProductInventoryResponse> responses = inventories.stream()
                .map(inv -> toResponse(inv, productSummaries.get(inv.getProductId())))
                .toList();

        log.debug("inventory_list page={} size={} total={} categoryId={}", page, size, total, categoryId);
        return new PageResult<>(responses, total);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductInventoryResponse getByProductId(java.util.UUID productId) {
        Inventory inventory = inventoryRepositoryPort.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
        Map<UUID, ProductSummary> summaries = catalogServicePort.findProductSummaries(List.of(productId));
        return toResponse(inventory, summaries.get(productId));
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