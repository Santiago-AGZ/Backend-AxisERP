package com.axiserp.purchase.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.purchase.domain.model.Supplier;

public interface SupplierRepositoryPort {
    Optional<Supplier> findById(UUID id);
    Optional<Supplier> findByNit(String nit);
    boolean existsByNit(String nit);
    Supplier save(Supplier supplier);
    List<Supplier> findAllActive();
    List<Supplier> findAll();
}
