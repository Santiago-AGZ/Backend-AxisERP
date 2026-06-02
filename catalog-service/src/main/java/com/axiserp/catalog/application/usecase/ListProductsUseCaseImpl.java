package com.axiserp.catalog.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.domain.model.Category;
import com.axiserp.catalog.domain.model.PageResult;
import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.ports.input.ListProductsUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListProductsUseCaseImpl implements ListProductsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListProductsUseCaseImpl.class);

    private final ProductRepositoryPort productRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProductResponse> list(String search, String codigo, UUID categoryId, boolean includeInactive, int page, int size) {
        List<Product> products = productRepositoryPort.findByFilters(search, codigo, categoryId, includeInactive, page, size);
        long total = productRepositoryPort.countByFilters(search, codigo, categoryId, includeInactive);

        List<UUID> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .toList();

        Map<UUID, Category> categoryMap = categoryIds.stream()
                .map(id -> categoryRepositoryPort.findById(id).orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<ProductResponse> responses = products.stream()
                .map(p -> toResponse(p, categoryMap.get(p.getCategoryId())))
                .toList();

        return new PageResult<>(responses, total);
    }

    private ProductResponse toResponse(Product product, Category category) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .codigo(product.getCodigo())
                .category(category != null ? ProductResponse.CategorySummary.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build() : null)
                .purchasePrice(product.getPurchasePrice())
                .salePrice(product.getSalePrice())
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
