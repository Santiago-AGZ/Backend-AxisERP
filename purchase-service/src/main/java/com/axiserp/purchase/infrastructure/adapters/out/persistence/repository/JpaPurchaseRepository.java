package com.axiserp.purchase.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseEntity;

public interface JpaPurchaseRepository extends JpaRepository<PurchaseEntity, UUID> {
    List<PurchaseEntity> findBySupplierId(UUID supplierId);
    List<PurchaseEntity> findByStatusOrderByCreatedAtDesc(PurchaseEntity.PurchaseStatus status);
    boolean existsByPurchaseNumber(String purchaseNumber);

    @Query(value = """
            SELECT * FROM purchases
            WHERE (:search IS NULL OR LOWER(purchase_number) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR status = :status)
            ORDER BY created_at DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<PurchaseEntity> findByFilters(@Param("search") String search, @Param("status") String status,
                                       @Param("size") int size, @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM purchases
            WHERE (:search IS NULL OR LOWER(purchase_number) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR status = :status)
            """, nativeQuery = true)
    long countByFilters(@Param("search") String search, @Param("status") String status);
}
