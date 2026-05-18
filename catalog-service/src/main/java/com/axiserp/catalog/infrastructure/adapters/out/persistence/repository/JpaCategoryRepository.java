package com.axiserp.catalog.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.CategoryEntity;

public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findByName(String name);

    boolean existsByName(String name);

    List<CategoryEntity> findAllByOrderByNameAsc();
}
