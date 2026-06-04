package com.axiserp.inventory.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.inventory.application.dto.request.InitializeInventoryRequest;
import com.axiserp.inventory.application.dto.response.InventoryResponse;
import com.axiserp.inventory.application.dto.response.ProductSummary;
import com.axiserp.inventory.domain.exception.InventoryAlreadyInitializedException;
import com.axiserp.inventory.domain.exception.InvalidStockConfigException;
import com.axiserp.inventory.application.service.AuditService;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.domain.exception.ProductNotActiveException;
import com.axiserp.inventory.ports.input.InitializeInventoryUseCase;
import com.axiserp.inventory.ports.output.CatalogServicePort;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InitializeInventoryUseCaseImpl implements InitializeInventoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(InitializeInventoryUseCaseImpl.class);

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final CatalogServicePort catalogServicePort;
    private final AuditService auditService;

    @Override
    @Transactional
    public InventoryResponse initialize(InitializeInventoryRequest request, UUID createdBy) {
        validateProductExistsAndIsActive(request.getProductId());

        if (inventoryRepositoryPort.findByProductId(request.getProductId()).isPresent()) {
            throw new InventoryAlreadyInitializedException(request.getProductId());
        }

        if (request.getMaxStock() > 0 && request.getMaxStock() <= request.getMinStock()) {
            throw new InvalidStockConfigException(
                    "El stock maximo (" + request.getMaxStock() + ") debe ser mayor que el stock minimo (" + request.getMinStock() + ")");
        }

        Inventory inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(request.getProductId())
                .currentStock(request.getInitialStock())
                .minStock(request.getMinStock())
                .maxStock(request.getMaxStock())
                .createdBy(createdBy)
                .build();

        Inventory saved = inventoryRepositoryPort.save(inventory);

        auditService.logInitialize(saved.getProductId(), createdBy, request.getInitialStock());

        if (request.getInitialStock() > 0) {
            InventoryMovement movement = InventoryMovement.builder()
                    .inventoryId(saved.getId())
                    .productId(saved.getProductId())
                    .movementType(MovementType.INVENTARIO_INICIAL)
                    .quantity(request.getInitialStock())
                    .previousStock(0)
                    .newStock(request.getInitialStock())
                    .notes(request.getNotes())
                    .createdBy(createdBy)
                    .build();
            inventoryRepositoryPort.saveMovement(movement);
        }

        log.info("inventory_initialized productId={} initialStock={}", saved.getProductId(), saved.getCurrentStock());

        return toResponse(saved);
    }

    private void validateProductExistsAndIsActive(UUID productId) {
        var summaries = catalogServicePort.findProductSummaries(List.of(productId));
        ProductSummary summary = summaries.get(productId);
        if (summary == null) {
            throw new ProductNotActiveException(productId, "NO_ENCONTRADO");
        }
        if (!"ACTIVO".equals(summary.getStatus())) {
            throw new ProductNotActiveException(productId, summary.getStatus());
        }
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .currentStock(inventory.getCurrentStock())
                .minStock(inventory.getMinStock())
                .maxStock(inventory.getMaxStock() != null ? inventory.getMaxStock() : 0)
                .lowStock(inventory.isLowStock())
                .depleted(inventory.isDepleted())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
