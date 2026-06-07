package com.axiserp.sales.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.CustomerEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.CustomerEntity.CustomerStatus;

public interface JpaCustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    @Query("""
            SELECT COUNT(c) FROM CustomerEntity c
            WHERE (:includeInactive = true OR c.status = :active)
              AND (:hasSearch = false
                   OR LOWER(c.name) LIKE :pattern
                   OR LOWER(c.documentNumber) LIKE :pattern
                   OR LOWER(c.codigo) LIKE :pattern)
            """)
    long countByFilters(
            @Param("hasSearch") boolean hasSearch,
            @Param("pattern") String pattern,
            @Param("includeInactive") boolean includeInactive,
            @Param("active") CustomerStatus active);

    Optional<CustomerEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    // :hasSearch evita el problema de lower(bytea) con parámetros null
    // :pattern debe venir pre-formateado como '%texto%' o '%' desde el adapter
    @Query("""
            SELECT c FROM CustomerEntity c
            WHERE (:includeInactive = true OR c.status = :active)
              AND (:hasSearch = false
                   OR LOWER(c.name) LIKE :pattern
                   OR LOWER(c.documentNumber) LIKE :pattern
                   OR LOWER(c.codigo) LIKE :pattern)
            ORDER BY c.updatedAt DESC
            """)
    List<CustomerEntity> findByFilters(
            @Param("hasSearch") boolean hasSearch,
            @Param("pattern") String pattern,
            @Param("includeInactive") boolean includeInactive,
            @Param("active") CustomerStatus active,
            Pageable pageable);
}
