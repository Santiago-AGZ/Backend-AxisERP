package com.axiserp.report.infrastructure.adapters.out.persistence.repository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ReportEntity;
public interface JpaReportRepository extends JpaRepository<ReportEntity, UUID> {}