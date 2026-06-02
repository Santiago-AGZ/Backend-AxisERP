package com.axiserp.report.ports.output;
import com.axiserp.report.domain.model.Report;
public interface ReportRepositoryPort {
    Report generateReport(Report report);
}