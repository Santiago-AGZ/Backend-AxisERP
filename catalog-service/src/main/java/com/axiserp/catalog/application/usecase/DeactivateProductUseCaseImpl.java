package com.axiserp.catalog.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.domain.exception.ProductNotFoundException;
import com.axiserp.catalog.domain.factory.ProductFactory;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.ports.input.DeactivateProductUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeactivateProductUseCaseImpl implements DeactivateProductUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeactivateProductUseCaseImpl.class);

    private final ProductRepositoryPort productRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    @Transactional
    public ProductResponse deactivate(UUID id) {
        Product existing = productRepositoryPort.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        if (existing.isDeleted()) {
            throw new IllegalStateException("El producto ya esta eliminado");
        }

        if (!existing.isActive()) {
            throw new IllegalStateException("El producto ya esta inactivo");
        }

        Product deleted = ProductFactory.softDelete(existing);
        Product saved = productRepositoryPort.save(deleted);

        Category category = categoryRepositoryPort.findById(saved.getCategoryId())
                .orElseThrow(() -> new IllegalStateException("Categoria no encontrada"));

        log.info("product_deleted id={} codigo={}", saved.getId(), saved.getCodigo());

        return toResponse(saved, category);
    }

    private ProductResponse toResponse(Product product, Category category) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .codigo(product.getCodigo())
                .category(ProductResponse.CategorySummary.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .purchasePrice(product.getPurchasePrice())
                .salePrice(product.getSalePrice())
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
