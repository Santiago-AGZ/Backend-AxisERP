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

    @Query(value = """
            SELECT DISTINCT s.* FROM sales s
            LEFT JOIN sale_items i ON s.id = i.sale_id
            WHERE (CAST(:customerId AS text) IS NULL OR s.customer_id = CAST(:customerId AS uuid))
              AND (CAST(:status AS text) IS NULL OR s.status = CAST(:status AS text))
              AND (CAST(:productId AS text) IS NULL OR i.product_id = CAST(:productId AS uuid))
              AND (CAST(:createdBy AS text) IS NULL OR s.created_by = CAST(:createdBy AS uuid))
              AND (CAST(:search AS text) IS NULL OR LOWER(s.sale_number) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s.id) FROM sales s
            LEFT JOIN sale_items i ON s.id = i.sale_id
            WHERE (CAST(:customerId AS text) IS NULL OR s.customer_id = CAST(:customerId AS uuid))
              AND (CAST(:status AS text) IS NULL OR s.status = CAST(:status AS text))
              AND (CAST(:productId AS text) IS NULL OR i.product_id = CAST(:productId AS uuid))
              AND (CAST(:createdBy AS text) IS NULL OR s.created_by = CAST(:createdBy AS uuid))
              AND (CAST(:search AS text) IS NULL OR LOWER(s.sale_number) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))
            """,
            nativeQuery = true)
    List<SaleEntity> findByFilters(
            @Param("customerId") UUID customerId,
            @Param("status") String status,
            @Param("productId") UUID productId,
            @Param("createdBy") UUID createdBy,
            @Param("search") String search,
            Pageable pageable);

    @Query(value = """
            SELECT COUNT(DISTINCT s.id) FROM sales s
            LEFT JOIN sale_items i ON s.id = i.sale_id
            WHERE (CAST(:customerId AS text) IS NULL OR s.customer_id = CAST(:customerId AS uuid))
              AND (CAST(:status AS text) IS NULL OR s.status = CAST(:status AS text))
              AND (CAST(:productId AS text) IS NULL OR i.product_id = CAST(:productId AS uuid))
              AND (CAST(:createdBy AS text) IS NULL OR s.created_by = CAST(:createdBy AS uuid))
              AND (CAST(:search AS text) IS NULL OR LOWER(s.sale_number) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))
            """, nativeQuery = true)
    long countByFilters(
            @Param("customerId") UUID customerId,
            @Param("status") String status,
            @Param("productId") UUID productId,
            @Param("createdBy") UUID createdBy,
            @Param("search") String search);

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
