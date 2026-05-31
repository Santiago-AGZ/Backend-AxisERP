package com.axiserp.auth.ports.output;

import java.util.Optional;
import java.util.UUID;

import com.axiserp.auth.domain.model.RefreshToken;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserIdAndToken(UUID userId, String token);
    void deleteByToken(String token);
    void deleteByUserId(UUID userId);
    void deleteExpired();
}
