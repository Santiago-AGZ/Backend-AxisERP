package com.axiserp.sales.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.SaleEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.SaleEntity.SaleStatus;

public interface JpaSaleRepository extends JpaRepository<SaleEntity, UUID> {

    @Query("""
            SELECT DISTINCT s FROM SaleEntity s
            LEFT JOIN s.items i
            WHERE (:customerId IS NULL OR s.customerId = :customerId)
              AND (:status IS NULL OR s.status = :status)
              AND (:productId IS NULL OR i.productId = :productId)
              AND (:createdBy IS NULL OR s.createdBy = :createdBy)
            ORDER BY s.updatedAt DESC
            """)
    List<SaleEntity> findByFilters(
            @Param("customerId") UUID customerId,
            @Param("status") SaleStatus status,
            @Param("productId") UUID productId,
            @Param("createdBy") UUID createdBy,
            Pageable pageable);

    Optional<SaleEntity> findBySaleNumber(String saleNumber);

    boolean existsByCustomerIdAndStatusIn(UUID customerId, List<SaleStatus> statuses);

    @Query("""
            SELECT s FROM SaleEntity s
            WHERE s.customerId = :customerId
              AND (:createdBy IS NULL OR s.createdBy = :createdBy)
            ORDER BY s.createdAt DESC
            """)
    List<SaleEntity> findByCustomerIdOrderByCreatedAtDesc(
            @Param("customerId") UUID customerId,
            @Param("createdBy") UUID createdBy);
}
