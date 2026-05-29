package com.axiserp.purchase.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.SupplierEntity;

public interface JpaSupplierRepository extends JpaRepository<SupplierEntity, UUID> {
    boolean existsByNit(String nit);
    Optional<SupplierEntity> findByNit(String nit);
    List<SupplierEntity> findByStatusOrderByNameAsc(SupplierEntity.SupplierStatus status);
}
