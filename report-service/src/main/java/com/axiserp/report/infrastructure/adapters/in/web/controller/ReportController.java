package com.axiserp.report.infrastructure.adapters.in.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.report.domain.model.ReportType;
import com.axiserp.report.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.report.infrastructure.adapters.in.web.dto.ApiResponse.PaginationMeta;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'INVENTARIO')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> listReportTypes() {
        List<String> types = List.of(
                ReportType.DAILY_SALES.name(),
                ReportType.INVENTORY_STATUS.name(),
                ReportType.TOP_PRODUCTS.name(),
                ReportType.CUSTOMER_FREQUENCY.name(),
                ReportType.DASHBOARD_SUMMARY.name()
        );
        return ResponseEntity.ok(ApiResponse.paged(
                types,
                "Tipos de reportes disponibles",
                PaginationMeta.of(1, types.size(), types.size())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'INVENTARIO')")
    @GetMapping("/{type}")
    public ResponseEntity<?> getReport(@PathVariable String type) {
        try {
            ReportType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(ApiResponse.ok(
                    "Reporte " + type + " en construcción",
                    "Reporte solicitado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("BAD_REQUEST", "Tipo de reporte no válido: " + type));
        }
    }
}
