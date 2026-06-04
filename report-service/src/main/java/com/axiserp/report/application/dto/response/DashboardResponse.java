package com.axiserp.report.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
    BigDecimal todayRevenue,
    long todaySalesCount,
    long pendingSalesCount,
    long lowStockCount,
    long totalCustomers,
    List<RecentSale> recentSales
) {
    public record RecentSale(UUID id, String saleNumber, BigDecimal total, String status, LocalDateTime createdAt) {}
}
