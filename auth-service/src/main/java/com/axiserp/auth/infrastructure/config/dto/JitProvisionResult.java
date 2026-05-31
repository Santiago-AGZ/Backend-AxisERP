package com.axiserp.auth.infrastructure.config.dto;

import com.axiserp.auth.domain.model.User;

public record JitProvisionResult(User user, boolean wasProvisioned) {}
