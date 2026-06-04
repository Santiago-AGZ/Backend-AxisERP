package com.axiserp.report.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SalesReportResponse(
    LocalDate startDate,
    LocalDate endDate,
    long totalSales,
    long totalTransactions,
    BigDecimal totalRevenue,
    BigDecimal totalTax,
    BigDecimal totalDiscount,
    Map<String, Long> salesByStatus,
    Map<String, Long> salesByUser,
    List<SaleSummary> recentSales
) {
    public record SaleSummary(UUID id, String saleNumber, String status, BigDecimal total, LocalDateTime createdAt) {}
}
