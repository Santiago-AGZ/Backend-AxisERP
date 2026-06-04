package com.axiserp.report.application.service;

import com.axiserp.report.application.dto.response.SalesReportResponse;
import com.axiserp.report.ports.input.GenerateSalesReportUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final GenerateSalesReportUseCase generateSalesReportUseCase;

    public byte[] exportSalesReport(LocalDate startDate, LocalDate endDate, String status, UUID userId, UUID clientId) {
        SalesReportResponse report = generateSalesReportUseCase.execute(startDate, endDate, status, userId, clientId);

        StringBuilder csv = new StringBuilder();
        csv.append("\"Numero\",\"Estado\",\"Total\",\"Impuesto\",\"Descuento\",\"Fecha\"\n");

        for (var sale : report.recentSales()) {
            csv.append(escapeCsv(sale.saleNumber())).append(",")
                    .append(escapeCsv(sale.status())).append(",")
                    .append(sale.total()).append(",")
                    .append(sale.createdAt() != null ? sale.createdAt().toLocalDate().toString() : "").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
