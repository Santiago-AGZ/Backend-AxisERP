package com.axiserp.catalog.application.usecase;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.application.dto.request.CreateProductRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.application.service.CatalogAuditService;
import com.axiserp.catalog.domain.exception.DuplicateCodigoException;
import com.axiserp.catalog.domain.exception.InvalidPriceException;
import com.axiserp.catalog.domain.factory.ProductFactory;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.ports.input.CreateProductUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProductUseCaseImpl implements CreateProductUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateProductUseCaseImpl.class);

    private final ProductRepositoryPort productRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final CatalogAuditService catalogAuditService;

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request, UUID createdBy) {
        if (productRepositoryPort.existsByCodigo(request.getCodigo())) {
            throw new DuplicateCodigoException();
        }

        if (request.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero");
        }
        if (request.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo");
        }
        if (request.getSalePrice().compareTo(request.getPurchasePrice()) < 0) {
            throw new InvalidPriceException();
        }

        Category category = categoryRepositoryPort.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));

        if (!category.isActive()) {
            throw new IllegalStateException(
                    "No se pueden crear productos en una categoria " + category.getStatus().name().toLowerCase());
        }

        Product product = ProductFactory.createNew(
                request.getName(),
                request.getCodigo(),
                request.getDescription(),
                category.getId(),
                request.getPurchasePrice(),
                request.getSalePrice(),
                createdBy);

        Product saved = productRepositoryPort.save(product);

        log.info("product_created id={} codigo={} category={}", saved.getId(), saved.getCodigo(), category.getName());
        catalogAuditService.log("CREATE", "PRODUCT", saved.getId(), createdBy, "Producto creado: " + saved.getName());

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
