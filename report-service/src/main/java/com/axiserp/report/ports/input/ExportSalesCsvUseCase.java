package com.axiserp.report.ports.input;

import java.time.LocalDate;
import java.util.UUID;

public interface ExportSalesCsvUseCase {

    byte[] exportSalesCsv(LocalDate startDate, LocalDate endDate, String status, UUID userId, UUID clientId);
}
