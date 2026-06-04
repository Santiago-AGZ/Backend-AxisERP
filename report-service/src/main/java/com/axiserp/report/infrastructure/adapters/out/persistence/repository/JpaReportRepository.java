package com.axiserp.report.infrastructure.adapters.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ReportCacheEntity;

public interface JpaReportRepository extends JpaRepository<ReportCacheEntity, UUID> {}