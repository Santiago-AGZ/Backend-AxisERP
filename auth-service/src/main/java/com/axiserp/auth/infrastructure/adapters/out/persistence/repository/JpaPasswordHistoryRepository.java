package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.PasswordHistoryEntity;

public interface JpaPasswordHistoryRepository extends JpaRepository<PasswordHistoryEntity, UUID> {

    List<PasswordHistoryEntity> findTop5ByUserIdOrderByCreatedAtDesc(UUID userId);
}
