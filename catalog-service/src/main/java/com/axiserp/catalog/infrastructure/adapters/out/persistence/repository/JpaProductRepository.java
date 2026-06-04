package com.axiserp.catalog.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.ProductEntity;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    int countByCategoryId(UUID categoryId);

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.categoryId = :categoryId AND p.status = 'ACTIVO'")
    int countActiveByCategoryId(@Param("categoryId") UUID categoryId);

    @Query(value = """
            SELECT * FROM products
            WHERE (:search IS NULL OR name ILIKE CONCAT('%', :search, '%'))
              AND (:codigo IS NULL OR codigo = :codigo)
              AND (:categoryId IS NULL OR CAST(category_id AS VARCHAR) = :categoryId)
              AND (:includeInactive = true OR status = 'ACTIVO')
            ORDER BY updated_at DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<ProductEntity> findByFilters(
            @Param("search") String search,
            @Param("codigo") String codigo,
            @Param("categoryId") String categoryId,
            @Param("includeInactive") boolean includeInactive,
            @Param("size") int size,
            @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM products
            WHERE (:search IS NULL OR name ILIKE CONCAT('%', :search, '%'))
              AND (:codigo IS NULL OR codigo = :codigo)
              AND (:categoryId IS NULL OR CAST(category_id AS VARCHAR) = :categoryId)
              AND (:includeInactive = true OR status = 'ACTIVO')
            """, nativeQuery = true)
    long countByFilters(
            @Param("search") String search,
            @Param("codigo") String codigo,
            @Param("categoryId") String categoryId,
            @Param("includeInactive") boolean includeInactive);
}
