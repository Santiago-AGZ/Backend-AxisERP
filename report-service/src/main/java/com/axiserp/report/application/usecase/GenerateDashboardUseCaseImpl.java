package com.axiserp.report.application.usecase;

import com.axiserp.report.application.dto.response.DashboardResponse;
import com.axiserp.report.application.dto.response.DashboardResponse.RecentSale;
import com.axiserp.report.ports.input.GenerateDashboardUseCase;
import com.axiserp.report.ports.output.InventoryServicePort;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GenerateDashboardUseCaseImpl implements GenerateDashboardUseCase {

    private final SalesServicePort salesServicePort;
    private final InventoryServicePort inventoryServicePort;

    @Override
    public DashboardResponse execute() {
        LocalDate today = LocalDate.now();

        // Fetch today's sales
        List<JsonNode> todaySales = fetchTodaySales(today);
        BigDecimal todayRevenue = BigDecimal.ZERO;
        long pendingSalesCount = 0;
        List<RecentSale> recentSales = new ArrayList<>();

        for (JsonNode sale : todaySales) {
            String status = sale.get("status").asText("");
            BigDecimal total = new BigDecimal(sale.get("total").asText("0"));

            if (!"ANULADA".equals(status)) {
                todayRevenue = todayRevenue.add(total);
            }

            if ("PENDIENTE".equals(status)) {
                pendingSalesCount++;
            }

            if (recentSales.size() < 10) {
                String createdAtStr = sale.has("createdAt") && !sale.get("createdAt").isNull()
                        ? sale.get("createdAt").asText() : null;
                recentSales.add(new RecentSale(
                        UUID.fromString(sale.get("id").asText()),
                        sale.get("saleNumber").asText(""),
                        total,
                        status,
                        createdAtStr != null ? LocalDateTime.parse(createdAtStr.replace(" ", "T")) : LocalDateTime.now()
                ));
            }
        }

        recentSales.sort(Comparator.comparing(RecentSale::createdAt).reversed());

        // Fetch low stock alerts
        long lowStockCount = 0;
        JsonNode alertResponse = inventoryServicePort.fetchAlerts(1, 100);
        if (alertResponse != null) {
            JsonNode alertData = alertResponse.get("data");
            if (alertData != null && alertData.isArray()) {
                lowStockCount = StreamSupport.stream(alertData.spliterator(), false).count();
            }
        }

        // Fetch customers count
        long totalCustomers = 0;
        JsonNode customerResponse = salesServicePort.fetchCustomers(null, false, 1, 1);
        if (customerResponse != null) {
            JsonNode pagination = customerResponse.get("pagination");
            if (pagination != null) {
                totalCustomers = pagination.get("totalRecords").asLong(0);
            }
        }

        return new DashboardResponse(
                todayRevenue,
                todaySales.size(),
                pendingSalesCount,
                lowStockCount,
                totalCustomers,
                recentSales.size() > 10 ? recentSales.subList(0, 10) : recentSales
        );
    }

    private List<JsonNode> fetchTodaySales(LocalDate today) {
        List<JsonNode> allSales = new ArrayList<>();
        for (int page = 0; page < 5; page++) {
            JsonNode response = salesServicePort.fetchSales(today, today, null, null, null, page + 1, 50);
            if (response == null) break;

            JsonNode data = response.get("data");
            if (data == null || !data.isArray() || data.isEmpty()) break;

            StreamSupport.stream(data.spliterator(), false).forEach(allSales::add);

            JsonNode pagination = response.get("pagination");
            if (pagination == null) break;

            int totalPages = pagination.has("totalPages") ? pagination.get("totalPages").asInt() : 1;
            if (page >= totalPages - 1) break;
        }
        return allSales;
    }
}
