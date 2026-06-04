package com.axiserp.report.ports.input;

import com.axiserp.report.application.dto.response.FrequentCustomerReportResponse;

import java.time.LocalDate;

public interface GenerateFrequentCustomersReportUseCase {
    FrequentCustomerReportResponse execute(LocalDate startDate, LocalDate endDate, int limit);
}
