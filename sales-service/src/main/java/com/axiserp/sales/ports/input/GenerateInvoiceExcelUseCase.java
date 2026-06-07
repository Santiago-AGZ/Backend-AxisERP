package com.axiserp.sales.ports.input;

import java.util.UUID;

public interface GenerateInvoiceExcelUseCase {

    byte[] generateInvoiceExcel(UUID saleId);
}
