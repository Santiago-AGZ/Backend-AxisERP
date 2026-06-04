package com.axiserp.catalog.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.application.dto.request.UpdateProductRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.application.service.CatalogAuditService;
import com.axiserp.catalog.domain.exception.DuplicateCodigoException;
import com.axiserp.catalog.domain.exception.InvalidPriceException;
import com.axiserp.catalog.domain.exception.ProductNotFoundException;
import com.axiserp.catalog.domain.factory.ProductFactory;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.ports.input.UpdateProductUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProductUseCaseImpl implements UpdateProductUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateProductUseCaseImpl.class);

    private final ProductRepositoryPort productRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final CatalogAuditService catalogAuditService;

    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request, UUID userId) {
        Product existing = productRepositoryPort.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        if (request.getPurchasePrice() != null && request.getSalePrice() != null) {
            if (request.getSalePrice().compareTo(request.getPurchasePrice()) < 0) {
                throw new InvalidPriceException();
            }
        } else if (request.getSalePrice() != null) {
            if (request.getSalePrice().compareTo(existing.getPurchasePrice()) < 0) {
                throw new InvalidPriceException();
            }
        } else if (request.getPurchasePrice() != null) {
            if (existing.getSalePrice().compareTo(request.getPurchasePrice()) < 0) {
                throw new InvalidPriceException();
            }
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepositoryPort.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        }

        Product updated = ProductFactory.update(
                existing,
                request.getName(),
                request.getDescription(),
                request.getCategoryId() != null ? request.getCategoryId() : existing.getCategoryId(),
                request.getPurchasePrice(),
                request.getSalePrice());

        Product saved = productRepositoryPort.save(updated);

        if (category == null) {
            category = categoryRepositoryPort.findById(saved.getCategoryId())
                    .orElseThrow(() -> new IllegalStateException("Categoria no encontrada"));
        }

        log.info("product_updated id={} codigo={}", saved.getId(), saved.getCodigo());
        catalogAuditService.log("UPDATE", "PRODUCT", saved.getId(), userId, "Producto actualizado: " + saved.getName());

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
