package com.axiserp.auth.ports.output;

import java.util.Optional;
import java.util.UUID;

import com.axiserp.auth.domain.model.TokenBlacklist;

public interface TokenBlacklistRepositoryPort {
    TokenBlacklist save(TokenBlacklist tokenBlacklist);
    Optional<TokenBlacklist> findByTokenJti(String tokenJti);
    Optional<TokenBlacklist> findByToken(String token);
    boolean existsByTokenJti(String tokenJti);
    boolean isTokenBlacklisted(String token);
    void deleteExpired();
}
