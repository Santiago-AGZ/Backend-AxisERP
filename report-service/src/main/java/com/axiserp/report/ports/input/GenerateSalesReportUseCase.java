package com.axiserp.report.ports.input;

import com.axiserp.report.application.dto.response.SalesReportResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface GenerateSalesReportUseCase {
    SalesReportResponse execute(LocalDate startDate, LocalDate endDate, String status, UUID userId, UUID clientId);
}
