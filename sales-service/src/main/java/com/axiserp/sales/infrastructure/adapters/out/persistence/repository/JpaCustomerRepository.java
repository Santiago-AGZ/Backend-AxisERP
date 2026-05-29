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

    Optional<CustomerEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByEmail(String email);

    @Query("""
            SELECT c FROM CustomerEntity c
            WHERE (:includeInactive = true OR c.status = :active)
              AND (:search IS NULL
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(c.documentNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(c.codigo) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY c.updatedAt DESC
            """)
    List<CustomerEntity> findByFilters(
            @Param("search") String search,
            @Param("includeInactive") boolean includeInactive,
            @Param("active") CustomerStatus active,
            Pageable pageable);
}
