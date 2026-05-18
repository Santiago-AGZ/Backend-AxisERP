package com.axiserp.catalog.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.ProductEntity;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    int countByCategoryId(UUID categoryId);

    @Query("SELECT p FROM ProductEntity p WHERE "
            + "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
            + "(:codigo IS NULL OR p.codigo = :codigo) AND "
            + "(:categoryId IS NULL OR p.categoryId = :categoryId) AND "
            + "(:includeInactive = true OR p.status = 'ACTIVO')")
    List<ProductEntity> findByFilters(
            @Param("search") String search,
            @Param("codigo") String codigo,
            @Param("categoryId") UUID categoryId,
            @Param("includeInactive") boolean includeInactive,
            PageRequest pageable);
}
