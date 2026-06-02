package com.axiserp.sales.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.sales.domain.model.Sale;

public interface SaleRepositoryPort {

    Optional<Sale> findById(UUID id);

    Sale save(Sale sale);

    /**
     * @param status nullable string representing SaleStatus name, e.g. "CONFIRMADA"
     */
    List<Sale> findByFilters(UUID customerId, String status, int page, int size);
}
