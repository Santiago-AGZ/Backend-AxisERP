package com.axiserp.sales.infrastructure.adapters.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.InvoiceEntity;

public interface JpaInvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {

    Optional<InvoiceEntity> findBySaleId(UUID saleId);
}
