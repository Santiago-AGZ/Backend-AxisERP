package com.axiserp.purchase.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.SupplierEntity;

public interface JpaSupplierRepository extends JpaRepository<SupplierEntity, UUID> {
    Optional<SupplierEntity> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    boolean existsByNit(String nit);
    boolean existsByNitAndIdNot(String nit, UUID id);
    Optional<SupplierEntity> findByNit(String nit);
    List<SupplierEntity> findByStatusOrderByNameAsc(SupplierEntity.SupplierStatus status);
    long countByStatus(SupplierEntity.SupplierStatus status);

    @Query(value = """
            SELECT * FROM suppliers
            WHERE status = 'ACTIVO'
              AND (:search IS NULL OR LOWER(name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(codigo) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(nit) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY name ASC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<SupplierEntity> findBySearch(@Param("search") String search, @Param("size") int size, @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM suppliers
            WHERE status = 'ACTIVO'
              AND (:search IS NULL OR LOWER(name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(codigo) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(nit) LIKE LOWER(CONCAT('%', :search, '%')))
            """, nativeQuery = true)
    long countBySearch(@Param("search") String search);
}
