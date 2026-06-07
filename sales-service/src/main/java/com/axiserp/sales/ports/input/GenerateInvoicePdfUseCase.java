package com.axiserp.sales.ports.input;

import java.util.UUID;

public interface GenerateInvoicePdfUseCase {

    byte[] generateInvoicePdf(UUID saleId);
}
