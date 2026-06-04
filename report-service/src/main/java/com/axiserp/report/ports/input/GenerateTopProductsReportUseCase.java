package com.axiserp.report.ports.input;

import com.axiserp.report.application.dto.response.TopProductsReportResponse;

import java.time.LocalDate;

public interface GenerateTopProductsReportUseCase {
    TopProductsReportResponse execute(LocalDate startDate, LocalDate endDate, int limit);
}
