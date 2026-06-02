package com.axiserp.sales.ports.input;

import java.util.UUID;

import com.axiserp.sales.application.dto.response.InvoiceResponse;

public interface GetInvoiceUseCase {

    InvoiceResponse getById(UUID id);

    InvoiceResponse getBySaleId(UUID saleId);
}
