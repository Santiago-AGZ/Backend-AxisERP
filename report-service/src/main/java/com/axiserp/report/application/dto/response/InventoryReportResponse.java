package com.axiserp.report.application.dto.response;

import java.util.List;
import java.util.UUID;

public record InventoryReportResponse(
    long totalProducts,
    long lowStockCount,
    long depletedCount,
    List<InventoryItem> items
) {
    public record InventoryItem(UUID productId, String productName, int currentStock, int minStock, boolean lowStock, boolean depleted) {}
}
