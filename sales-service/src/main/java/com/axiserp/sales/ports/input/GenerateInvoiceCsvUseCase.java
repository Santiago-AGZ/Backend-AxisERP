package com.axiserp.sales.ports.input;

import java.util.UUID;

public interface GenerateInvoiceCsvUseCase {

    byte[] generateInvoiceCsv(UUID saleId);
}
