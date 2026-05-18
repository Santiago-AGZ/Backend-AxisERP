package com.axiserp.catalog.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.catalog.domain.model.Product;

public interface ProductRepositoryPort {

    Optional<Product> findById(UUID id);

    Optional<Product> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    Product save(Product product);

    List<Product> findByFilters(String search, String codigo, UUID categoryId, boolean includeInactive, int page, int size);

    int countByCategoryId(UUID categoryId);
}
