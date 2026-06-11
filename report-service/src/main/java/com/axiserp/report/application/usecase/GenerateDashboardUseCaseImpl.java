package com.axiserp.report.application.usecase;

import com.axiserp.report.application.dto.response.DashboardResponse;
import com.axiserp.report.application.dto.response.DashboardResponse.RecentSale;
import com.axiserp.report.ports.input.GenerateDashboardUseCase;
import com.axiserp.report.ports.output.InventoryServicePort;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GenerateDashboardUseCaseImpl implements GenerateDashboardUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerateDashboardUseCaseImpl.class);
    private static final int MAX_SALES_PAGES = 2;
    private static final int RECENT_SALES_LIMIT = 10;
    private static final int TIMEOUT_SECONDS = 3;

    private final SalesServicePort salesServicePort;
    private final InventoryServicePort inventoryServicePort;

    @Override
    public DashboardResponse execute() {
        long start = System.currentTimeMillis();
        LocalDate today = LocalDate.now();

        // Step 1: Fetch today's sales (sequential — depends on pagination)
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
            if (recentSales.size() < RECENT_SALES_LIMIT) {
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

        // Step 2: Fetch alerts and customers count IN PARALLEL
        CompletableFuture<Long> futureLowStock = CompletableFuture.supplyAsync(() -> fetchLowStockCount());
        CompletableFuture<Long> futureTotalCustomers = CompletableFuture.supplyAsync(() -> fetchTotalCustomers());

        long lowStockCount = 0;
        long totalCustomers = 0;

        try {
            lowStockCount = futureLowStock.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("dashboard_lowstock_timeout: {}", e.getMessage());
        }

        try {
            totalCustomers = futureTotalCustomers.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("dashboard_customers_timeout: {}", e.getMessage());
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("dashboard_generated elapsed={}ms revenue={} sales={} pending={} lowStock={} customers={}",
                elapsed, todayRevenue, todaySales.size(), pendingSalesCount, lowStockCount, totalCustomers);

        return new DashboardResponse(
                todayRevenue,
                todaySales.size(),
                pendingSalesCount,
                lowStockCount,
                totalCustomers,
                recentSales.size() > RECENT_SALES_LIMIT ? recentSales.subList(0, RECENT_SALES_LIMIT) : recentSales
        );
    }

    private List<JsonNode> fetchTodaySales(LocalDate today) {
        List<JsonNode> allSales = new ArrayList<>();
        for (int page = 0; page < MAX_SALES_PAGES; page++) {
            try {
                JsonNode response = salesServicePort.fetchSales(today, today, null, null, null, page + 1, 50);
                if (response == null) break;

                JsonNode data = response.get("data");
                if (data == null || !data.isArray() || data.isEmpty()) break;

                data.forEach(allSales::add);

                JsonNode pagination = response.get("pagination");
                if (pagination == null) break;

                int totalPages = pagination.has("totalPages") ? pagination.get("totalPages").asInt() : 1;
                if (page >= totalPages - 1) break;
            } catch (Exception e) {
                log.warn("dashboard_fetch_sales_page_{}_failed: {}", page + 1, e.getMessage());
                break;
            }
        }
        return allSales;
    }

    private long fetchLowStockCount() {
        try {
            JsonNode alertResponse = inventoryServicePort.fetchAlerts(1, 100);
            if (alertResponse != null) {
                JsonNode alertData = alertResponse.get("data");
                if (alertData != null && alertData.isArray()) {
                    return StreamSupport.stream(alertData.spliterator(), false).count();
                }
            }
        } catch (Exception e) {
            log.warn("dashboard_fetch_lowstock_failed: {}", e.getMessage());
        }
        return 0;
    }

    private long fetchTotalCustomers() {
        try {
            JsonNode customerResponse = salesServicePort.fetchCustomers(null, false, 1, 1);
            if (customerResponse != null) {
                JsonNode pagination = customerResponse.get("pagination");
                if (pagination != null) {
                    return pagination.get("totalRecords").asLong(0);
                }
            }
        } catch (Exception e) {
            log.warn("dashboard_fetch_customers_failed: {}", e.getMessage());
        }
        return 0;
    }
}
