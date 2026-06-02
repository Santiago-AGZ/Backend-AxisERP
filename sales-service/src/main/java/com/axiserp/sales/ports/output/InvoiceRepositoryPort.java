package com.axiserp.sales.ports.output;

import java.util.Optional;
import java.util.UUID;

import com.axiserp.sales.domain.model.Invoice;

public interface InvoiceRepositoryPort {

    Optional<Invoice> findById(UUID id);

    Optional<Invoice> findBySaleId(UUID saleId);

    Invoice save(Invoice invoice);
}
