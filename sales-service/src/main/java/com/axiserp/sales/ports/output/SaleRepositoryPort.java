package com.axiserp.sales.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.sales.domain.model.Sale;

public interface SaleRepositoryPort {

    Optional<Sale> findById(UUID id);

    Sale save(Sale sale);

    boolean existsPendingByCustomerId(UUID customerId);

    List<Sale> findByCustomerId(UUID customerId);

    /**
     * @param status nullable string representing SaleStatus name, e.g. "CONFIRMADA"
     * @param productId nullable UUID to filter by product
     */
    List<Sale> findByFilters(UUID customerId, String status, UUID productId, int page, int size);
}
