package com.axiserp.catalog.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.domain.model.Product.ProductStatus;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.ProductEntity;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.repository.JpaProductRepository;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final JpaProductRepository jpaProductRepository;

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaProductRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Product> findByCodigo(String codigo) {
        return jpaProductRepository.findByCodigo(codigo).map(this::toDomain);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return jpaProductRepository.existsByCodigo(codigo);
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = toEntity(product);
        ProductEntity saved = jpaProductRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Product> findByFilters(String search, String codigo, UUID categoryId, boolean includeInactive, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return jpaProductRepository.findByFilters(search, codigo, categoryId, includeInactive, pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int countByCategoryId(UUID categoryId) {
        return jpaProductRepository.countByCategoryId(categoryId);
    }

    private Product toDomain(ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .codigo(entity.getCodigo())
                .categoryId(entity.getCategoryId())
                .purchasePrice(entity.getPurchasePrice())
                .salePrice(entity.getSalePrice())
                .status(ProductStatus.valueOf(entity.getStatus().name()))
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ProductEntity toEntity(Product domain) {
        return ProductEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .codigo(domain.getCodigo())
                .categoryId(domain.getCategoryId())
                .purchasePrice(domain.getPurchasePrice())
                .salePrice(domain.getSalePrice())
                .status(ProductEntity.ProductStatus.valueOf(domain.getStatus().name()))
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
