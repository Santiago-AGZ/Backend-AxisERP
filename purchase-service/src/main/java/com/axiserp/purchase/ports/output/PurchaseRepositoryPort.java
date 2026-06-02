package com.axiserp.purchase.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.purchase.domain.model.Purchase;

public interface PurchaseRepositoryPort {
    Optional<Purchase> findById(UUID id);
    Purchase save(Purchase purchase);
    List<Purchase> findAll();
    List<Purchase> findAll(String search, String status, int page, int size);
    long countAll();
    long countByFilters(String search, String status);
    List<Purchase> findBySupplierId(UUID supplierId);
}
