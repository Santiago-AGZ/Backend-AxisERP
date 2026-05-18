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

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request, UUID createdBy) {
        if (productRepositoryPort.existsByCodigo(request.getCodigo())) {
            throw new DuplicateCodigoException();
        }

        if (request.getSalePrice().compareTo(request.getPurchasePrice()) < 0) {
            throw new InvalidPriceException();
        }

        Category category = categoryRepositoryPort.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));

        Product product = ProductFactory.createNew(
                request.getName(),
                request.getCodigo(),
                category.getId(),
                request.getPurchasePrice(),
                request.getSalePrice(),
                createdBy);

        Product saved = productRepositoryPort.save(product);

        log.info("product_created id={} codigo={} category={}", saved.getId(), saved.getCodigo(), category.getName());

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
