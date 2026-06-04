package com.axiserp.report.application.usecase;

import com.axiserp.report.application.dto.response.SalesReportResponse;
import com.axiserp.report.application.dto.response.SalesReportResponse.SaleSummary;
import com.axiserp.report.ports.input.GenerateSalesReportUseCase;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GenerateSalesReportUseCaseImpl implements GenerateSalesReportUseCase {

    private final SalesServicePort salesServicePort;

    private static final int MAX_PAGES = 20;
    private static final int PAGE_SIZE = 50;

    @Override
    public SalesReportResponse execute(LocalDate startDate, LocalDate endDate, String status, UUID userId, UUID clientId) {
        List<JsonNode> allSales = fetchAllSales(startDate, endDate, status, userId, clientId);

        long totalTransactions = allSales.size();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        Map<String, Long> salesByStatus = new HashMap<>();
        Map<String, Long> salesByUser = new HashMap<>();
        List<SaleSummary> recentSales = new ArrayList<>();

        for (JsonNode sale : allSales) {
            String saleStatus = sale.get("status").asText("");
            BigDecimal saleTotal = new BigDecimal(sale.get("total").asText("0"));
            BigDecimal saleTax = new BigDecimal(sale.get("tax").asText("0"));
            BigDecimal saleDiscount = new BigDecimal(sale.get("discount").asText("0"));

            totalRevenue = totalRevenue.add(saleTotal);
            totalTax = totalTax.add(saleTax);
            totalDiscount = totalDiscount.add(saleDiscount);

            salesByStatus.merge(saleStatus, 1L, Long::sum);

            if (sale.has("createdBy") && !sale.get("createdBy").isNull()) {
                String createdBy = sale.get("createdBy").asText();
                salesByUser.merge(createdBy, 1L, Long::sum);
            }

            if (recentSales.size() < 20) {
                String createdAtStr = sale.has("createdAt") && !sale.get("createdAt").isNull()
                        ? sale.get("createdAt").asText() : null;
                recentSales.add(new SaleSummary(
                        UUID.fromString(sale.get("id").asText()),
                        sale.get("saleNumber").asText(""),
                        saleStatus,
                        saleTotal,
                        createdAtStr != null ? LocalDateTime.parse(createdAtStr.replace(" ", "T")) : LocalDateTime.now()
                ));
            }
        }

        recentSales.sort(Comparator.comparing(SaleSummary::createdAt).reversed());

        return new SalesReportResponse(
                startDate, endDate,
                allSales.size(), totalTransactions,
                totalRevenue, totalTax, totalDiscount,
                salesByStatus, salesByUser,
                recentSales.size() > 20 ? recentSales.subList(0, 20) : recentSales
        );
    }

    private List<JsonNode> fetchAllSales(LocalDate startDate, LocalDate endDate, String status, UUID userId, UUID clientId) {
        List<JsonNode> allSales = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            JsonNode response = salesServicePort.fetchSales(startDate, endDate, status, userId, clientId, page + 1, PAGE_SIZE);
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
