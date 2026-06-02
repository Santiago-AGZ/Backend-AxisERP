package com.axiserp.report.infrastructure.adapters.out.persistence.adapter;
import org.springframework.stereotype.Component;
import com.axiserp.report.domain.model.Report;
import com.axiserp.report.ports.output.ReportRepositoryPort;
import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ReportEntity;
import com.axiserp.report.infrastructure.adapters.out.persistence.repository.JpaReportRepository;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class ReportRepositoryAdapter implements ReportRepositoryPort {
    private final JpaReportRepository jpaRepository;
    @Override
    public Report generateReport(Report report) {
        ReportEntity entity = ReportEntity.builder()
            .name(report.getName())
            .data("{}")
            .build();
        entity = jpaRepository.save(entity);
        report.setId(entity.getId());
        return report;
    }
}