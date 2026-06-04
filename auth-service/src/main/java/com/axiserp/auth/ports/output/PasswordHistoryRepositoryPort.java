package com.axiserp.auth.ports.output;

import java.util.List;
import java.util.UUID;

import com.axiserp.auth.domain.model.PasswordHistory;

public interface PasswordHistoryRepositoryPort {

    List<PasswordHistory> findLastByUserId(UUID userId, int limit);

    PasswordHistory save(PasswordHistory passwordHistory);
}
