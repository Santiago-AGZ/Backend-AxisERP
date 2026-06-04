package com.axiserp.purchase.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.purchase.domain.model.Supplier;

public interface SupplierRepositoryPort {
    Optional<Supplier> findById(UUID id);
    Optional<Supplier> findByCodigo(String codigo);
    Optional<Supplier> findByNit(String nit);
    boolean existsByCodigo(String codigo);
    boolean existsByNit(String nit);
    boolean existsByNitAndIdNot(String nit, UUID id);
    Supplier save(Supplier supplier);
    List<Supplier> findAllActive();
    List<Supplier> findAllActive(String search, int page, int size);
    long countAllActive();
    long countActiveBySearch(String search);
    List<Supplier> findAll();
}
