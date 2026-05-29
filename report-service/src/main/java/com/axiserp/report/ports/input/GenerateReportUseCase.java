package com.axiserp.report.ports.input;
import com.axiserp.report.domain.model.Report;
public interface GenerateReportUseCase {
    Report generateReport(Report report);
}