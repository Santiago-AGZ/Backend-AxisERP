package com.axiserp.report.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FrequentCustomerReportResponse(
    LocalDate startDate,
    LocalDate endDate,
    List<CustomerFrequency> customers
) {
    public record CustomerFrequency(
        int position,
        UUID customerId,
        String customerName,
        long totalVisits,
        BigDecimal totalSpent,
        BigDecimal averageTicket,
        LocalDateTime lastVisitDate
    ) {}
}
