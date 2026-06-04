package com.axiserp.catalog.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.CategoryEntity;

public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findByName(String name);

    boolean existsByName(String name);

    List<CategoryEntity> findAllByOrderByNameAsc();

    List<CategoryEntity> findByStatusOrderByStatusAscNameAsc(CategoryEntity.CategoryStatus status);

    @Query(value = """
            SELECT * FROM categories
            WHERE (:search IS NULL OR name ILIKE CONCAT('%', :search, '%'))
              AND (:includeInactive = true OR status = 'ACTIVA')
            ORDER BY name ASC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<CategoryEntity> findByFilters(
            @Param("search") String search,
            @Param("includeInactive") boolean includeInactive,
            @Param("size") int size,
            @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM categories
            WHERE (:search IS NULL OR name ILIKE CONCAT('%', :search, '%'))
              AND (:includeInactive = true OR status = 'ACTIVA')
            """, nativeQuery = true)
    long countByFilters(
            @Param("search") String search,
            @Param("includeInactive") boolean includeInactive);
}
