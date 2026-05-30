package com.axiserp.auth.ports.output;

import java.util.Optional;
import java.util.UUID;

import com.axiserp.auth.domain.model.OtpToken;

public interface OtpTokenRepositoryPort {
    OtpToken save(OtpToken otpToken);
    Optional<OtpToken> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
    void deleteExpired();
}
