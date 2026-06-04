package com.axiserp.report.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TopProductsReportResponse(
    LocalDate startDate,
    LocalDate endDate,
    List<ProductRank> rankings
) {
    public record ProductRank(int position, UUID productId, String productName, long totalQuantity, BigDecimal totalRevenue) {}
}
