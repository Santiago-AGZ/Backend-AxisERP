package com.axiserp.inventory.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryEntity;

public interface JpaInventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    Optional<InventoryEntity> findByProductId(UUID productId);
    Page<InventoryEntity> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM inventory WHERE current_stock <= min_stock ORDER BY current_stock ASC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<InventoryEntity> findLowStock(@Param("size") int size, @Param("offset") int offset);

    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.currentStock <= i.minStock")
    long countLowStock();

    @Query("SELECT i FROM InventoryEntity i WHERE i.productId IN :productIds ORDER BY i.currentStock ASC")
    List<InventoryEntity> findByProductIds(@Param("productIds") List<UUID> productIds, Pageable pageable);

    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.productId IN :productIds")
    long countByProductIds(@Param("productIds") List<UUID> productIds);

    @Query(value = "SELECT * FROM inventory WHERE current_stock = 0 ORDER BY updated_at DESC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<InventoryEntity> findDepleted(@Param("size") int size, @Param("offset") int offset);

    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.currentStock = 0")
    long countDepleted();
}
