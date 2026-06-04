package com.axiserp.catalog.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.catalog.domain.model.Category;

public interface CategoryRepositoryPort {

    Optional<Category> findById(UUID id);

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    Category save(Category category);

    List<Category> findAllOrderedByName();

    List<Category> findAllOrderedByName(int page, int size);

    List<Category> findByFilters(String search, boolean includeInactive, int page, int size);

    long countByFilters(String search, boolean includeInactive);

    long countAll();
}
