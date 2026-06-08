package com.axiserp.report.application.usecase;

import com.axiserp.report.application.dto.response.FrequentCustomerReportResponse;
import com.axiserp.report.application.dto.response.FrequentCustomerReportResponse.CustomerFrequency;
import com.axiserp.report.application.service.ReportAuditService;
import com.axiserp.report.ports.input.GenerateFrequentCustomersReportUseCase;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GenerateFrequentCustomersReportUseCaseImpl implements GenerateFrequentCustomersReportUseCase {

    private final SalesServicePort salesServicePort;
    private final ReportAuditService reportAuditService;

    private static final int MAX_PAGES = 20;
    private static final int PAGE_SIZE = 50;

    @Override
    public FrequentCustomerReportResponse execute(LocalDate startDate, LocalDate endDate, int limit) {
        List<JsonNode> allSales = fetchAllSales(startDate, endDate);
        Map<String, CustomerAggregation> customerMap = new HashMap<>();

        for (JsonNode sale : allSales) {
            String customerId = sale.has("customerId") && !sale.get("customerId").isNull()
                    ? sale.get("customerId").asText() : null;
            if (customerId == null || customerId.isBlank()) continue;

            BigDecimal total = new BigDecimal(sale.get("total").asText("0"));
            String createdAtStr = sale.has("createdAt") && !sale.get("createdAt").isNull()
                    ? sale.get("createdAt").asText() : null;
            LocalDateTime createdAt = createdAtStr != null
                    ? LocalDateTime.parse(createdAtStr.replace(" ", "T")) : null;

            customerMap.merge(customerId, new CustomerAggregation(
                    customerId, "", 1L, total, createdAt
            ), (a, b) -> new CustomerAggregation(
                    a.customerId(), a.customerName(),
                    a.totalVisits() + b.totalVisits(),
                    a.totalSpent().add(b.totalSpent()),
                    a.lastVisitDate() == null || (b.lastVisitDate() != null && b.lastVisitDate().isAfter(a.lastVisitDate()))
                            ? b.lastVisitDate() : a.lastVisitDate()
            ));
        }

        Map<String, String> customerNameMap = fetchCustomerNames();

        List<CustomerFrequency> ranked = customerMap.values().stream()
                .sorted((a, b) -> Long.compare(b.totalVisits(), a.totalVisits()))
                .limit(limit)
                .map(agg -> {
                    String name = customerNameMap.getOrDefault(agg.customerId(), agg.customerId());
                    BigDecimal avgTicket = agg.totalVisits() > 0
                            ? agg.totalSpent().divide(BigDecimal.valueOf(agg.totalVisits()), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new CustomerFrequency(0, UUID.fromString(agg.customerId()),
                            name, agg.totalVisits(), agg.totalSpent(), avgTicket, agg.lastVisitDate());
                })
                .toList();

        List<CustomerFrequency> result = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            CustomerFrequency c = ranked.get(i);
            result.add(new CustomerFrequency(i + 1, c.customerId(), c.customerName(),
                    c.totalVisits(), c.totalSpent(), c.averageTicket(), c.lastVisitDate()));
        }

        var response = new FrequentCustomerReportResponse(startDate, endDate, result);

        String filterParams = String.format("{\"startDate\":\"%s\",\"endDate\":\"%s\",\"limit\":%d}",
                startDate, endDate, limit);
        reportAuditService.logReportGeneration("CUSTOMER_FREQUENCY", "CSV", result.size(), filterParams);

        return response;
    }

    private Map<String, String> fetchCustomerNames() {
        Map<String, String> nameMap = new HashMap<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            JsonNode response = salesServicePort.fetchCustomers(null, false, page + 1, PAGE_SIZE);
            if (response == null) break;

            JsonNode data = response.get("data");
            if (data == null || !data.isArray() || data.isEmpty()) break;

            data.forEach(c -> {
                String id = c.has("id") && !c.get("id").isNull() ? c.get("id").asText() : null;
                String name = c.has("name") && !c.get("name").isNull() ? c.get("name").asText()
                        : (c.has("fullName") && !c.get("fullName").isNull() ? c.get("fullName").asText() : id);
                if (id != null) nameMap.put(id, name);
            });

            JsonNode pagination = response.get("pagination");
            if (pagination == null) break;

            int totalPages = pagination.has("totalPages") ? pagination.get("totalPages").asInt() : 1;
            if (page >= totalPages - 1) break;
        }
        return nameMap;
    }

    private record CustomerAggregation(String customerId, String customerName,
                                        long totalVisits, BigDecimal totalSpent,
                                        LocalDateTime lastVisitDate) {}

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
