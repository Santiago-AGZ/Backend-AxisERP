package com.axiserp.purchase.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseEntity;

public interface JpaPurchaseRepository extends JpaRepository<PurchaseEntity, UUID> {
    List<PurchaseEntity> findBySupplierId(UUID supplierId);
    List<PurchaseEntity> findByStatusOrderByCreatedAtDesc(PurchaseEntity.PurchaseStatus status);
    boolean existsByPurchaseNumber(String purchaseNumber);
}
