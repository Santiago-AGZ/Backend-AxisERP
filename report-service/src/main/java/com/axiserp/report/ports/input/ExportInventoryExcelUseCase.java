package com.axiserp.report.ports.input;

import java.util.UUID;

public interface ExportInventoryExcelUseCase {

    byte[] exportInventoryExcel(UUID categoryId);
}
