package com.axiserp.report.ports.input;

import java.time.LocalDate;
import java.util.UUID;

public interface ExportSalesPdfUseCase {

    byte[] exportSalesPdf(LocalDate startDate, LocalDate endDate, UUID clientId);
}
