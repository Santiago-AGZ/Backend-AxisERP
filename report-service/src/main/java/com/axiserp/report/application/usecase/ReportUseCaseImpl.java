package com.axiserp.report.application.usecase;
import org.springframework.stereotype.Service;
import com.axiserp.report.domain.model.Report;
import com.axiserp.report.ports.input.GenerateReportUseCase;
import com.axiserp.report.ports.output.ReportRepositoryPort;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ReportUseCaseImpl implements GenerateReportUseCase {
    private final ReportRepositoryPort repository;
    @Override
    public Report generateReport(Report report) {
        return repository.generateReport(report);
    }
}