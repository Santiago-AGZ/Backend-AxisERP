package com.axiserp.sales.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.SaleItemEntity;

public interface JpaSaleItemRepository extends JpaRepository<SaleItemEntity, UUID> {

    List<SaleItemEntity> findBySale_Id(UUID saleId);
}
