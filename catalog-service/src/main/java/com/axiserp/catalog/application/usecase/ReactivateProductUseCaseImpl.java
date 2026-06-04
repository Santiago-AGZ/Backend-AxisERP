package com.axiserp.catalog.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.application.service.CatalogAuditService;
import com.axiserp.catalog.domain.exception.ProductNotFoundException;
import com.axiserp.catalog.domain.factory.ProductFactory;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.ports.input.ReactivateProductUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReactivateProductUseCaseImpl implements ReactivateProductUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReactivateProductUseCaseImpl.class);

    private final ProductRepositoryPort productRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final CatalogAuditService catalogAuditService;

    @Override
    @Transactional
    public ProductResponse reactivate(UUID id, UUID userId) {
        Product existing = productRepositoryPort.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        if (existing.isDeleted()) {
            throw new IllegalStateException("No se puede reactivar un producto eliminado");
        }

        if (existing.isActive()) {
            throw new IllegalStateException("El producto ya esta activo");
        }

        Product reactivated = ProductFactory.reactivate(existing);
        Product saved = productRepositoryPort.save(reactivated);

        Category category = categoryRepositoryPort.findById(saved.getCategoryId())
                .orElseThrow(() -> new IllegalStateException("Categoria no encontrada"));

        log.info("product_reactivated id={} codigo={}", saved.getId(), saved.getCodigo());
        catalogAuditService.log("REACTIVATE", "PRODUCT", saved.getId(), userId, "Producto reactivado: " + saved.getName());

        return toResponse(saved, category);
    }

    private ProductResponse toResponse(Product product, Category category) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .codigo(product.getCodigo())
                .description(product.getDescription())
                .category(ProductResponse.CategorySummary.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .purchasePrice(product.getPurchasePrice())
                .salePrice(product.getSalePrice())
                .margin(product.getMargin())
                .marginPercentage(product.getMarginPercentage())
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
