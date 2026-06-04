package com.axiserp.report.application.usecase;

import com.axiserp.report.application.dto.response.TopProductsReportResponse;
import com.axiserp.report.application.dto.response.TopProductsReportResponse.ProductRank;
import com.axiserp.report.ports.input.GenerateTopProductsReportUseCase;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GenerateTopProductsReportUseCaseImpl implements GenerateTopProductsReportUseCase {

    private final SalesServicePort salesServicePort;

    private static final int MAX_PAGES = 20;
    private static final int PAGE_SIZE = 50;

    @Override
    public TopProductsReportResponse execute(LocalDate startDate, LocalDate endDate, int limit) {
        List<JsonNode> allSales = fetchAllSales(startDate, endDate);

        Map<String, ProductAggregation> productMap = new HashMap<>();

        for (JsonNode sale : allSales) {
            JsonNode items = sale.get("items");
            if (items == null || !items.isArray()) continue;

            for (JsonNode item : items) {
                String productId = item.get("productId").asText();
                long quantity = item.get("quantity").asLong(0);
                BigDecimal subtotal = new BigDecimal(item.get("subtotal").asText("0"));

                productMap.merge(productId, new ProductAggregation(
                        productId,
                        item.has("productName") ? item.get("productName").asText("") : "",
                        quantity,
                        subtotal
                ), (a, b) -> new ProductAggregation(
                        a.productId(), a.productName(),
                        a.totalQuantity() + b.totalQuantity(),
                        a.totalRevenue().add(b.totalRevenue())
                ));
            }
        }

        List<ProductRank> rankings = productMap.values().stream()
                .sorted((a, b) -> Long.compare(b.totalQuantity(), a.totalQuantity()))
                .limit(limit)
                .map(agg -> new ProductRank(0, UUID.fromString(agg.productId()),
                        agg.productName(), agg.totalQuantity(), agg.totalRevenue()))
                .toList();

        List<ProductRank> ranked = new ArrayList<>();
        for (int i = 0; i < rankings.size(); i++) {
            ProductRank p = rankings.get(i);
            ranked.add(new ProductRank(i + 1, p.productId(), p.productName(), p.totalQuantity(), p.totalRevenue()));
        }

        return new TopProductsReportResponse(startDate, endDate, ranked);
    }

    private record ProductAggregation(String productId, String productName, long totalQuantity, BigDecimal totalRevenue) {}

    private List<JsonNode> fetchAllSales(LocalDate startDate, LocalDate endDate) {
        List<JsonNode> allSales = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            JsonNode response = salesServicePort.fetchSales(startDate, endDate, null, null, null, page + 1, PAGE_SIZE);
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
