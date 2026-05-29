package com.axiserp.report.infrastructure.adapters.in.web.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.axiserp.report.domain.model.Report;
import com.axiserp.report.ports.input.GenerateReportUseCase;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final GenerateReportUseCase useCase;
    @PostMapping
    public ResponseEntity<Report> generateReport(@RequestBody Report report) {
        return ResponseEntity.ok(useCase.generateReport(report));
    }
}