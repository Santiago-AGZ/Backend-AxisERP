package com.axiserp.purchase.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseItemEntity;

public interface JpaPurchaseItemRepository extends JpaRepository<PurchaseItemEntity, UUID> {
    List<PurchaseItemEntity> findByPurchaseId(UUID purchaseId);
}
