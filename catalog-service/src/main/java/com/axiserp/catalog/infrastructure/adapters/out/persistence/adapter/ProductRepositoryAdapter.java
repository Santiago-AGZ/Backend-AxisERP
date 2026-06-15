package com.axiserp.catalog.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        jpaProductRepository.findById(product.getId()).ifPresent(existing ->
            entity.setVersion(existing.getVersion())
        );
        ProductEntity saved = jpaProductRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Product> findByFilters(String search, String codigo, UUID categoryId, boolean includeInactive, int page, int size) {
        int offset = page * size;
        String categoryIdStr = categoryId != null ? categoryId.toString() : null;
        String searchParam = (search != null && !search.isBlank()) ? search : null;
        String codigoParam = (codigo != null && !codigo.isBlank()) ? codigo : null;
        return jpaProductRepository.findByFilters(searchParam, codigoParam, categoryIdStr, includeInactive, size, offset)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByFilters(String search, String codigo, UUID categoryId, boolean includeInactive) {
        String categoryIdStr = categoryId != null ? categoryId.toString() : null;
        String searchParam = (search != null && !search.isBlank()) ? search : null;
        String codigoParam = (codigo != null && !codigo.isBlank()) ? codigo : null;
        return jpaProductRepository.countByFilters(searchParam, codigoParam, categoryIdStr, includeInactive);
    }

    @Override
    public int countByCategoryId(UUID categoryId) {
        return jpaProductRepository.countByCategoryId(categoryId);
    }

    @Override
    public int countActiveByCategoryId(UUID categoryId) {
        return jpaProductRepository.countActiveByCategoryId(categoryId);
    }

    private Product toDomain(ProductEntity entity) {
        Product product = Product.builder().createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
        product.setVersion(entity.getVersion());
        return product;
    }

    private ProductEntity toEntity(Product domain) {
        ProductEntity entity = ProductEntity.builder().createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
        entity.setVersion(domain.getVersion());
        return entity;
    }
}
