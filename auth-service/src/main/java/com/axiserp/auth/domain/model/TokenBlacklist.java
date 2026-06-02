package com.axiserp.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TokenBlacklist {
    private UUID id;
    private String tokenJti;
    private UUID userId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public TokenBlacklist(UUID id, String tokenJti, UUID userId, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.tokenJti = tokenJti;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public TokenBlacklist(String tokenJti, UUID userId, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.tokenJti = tokenJti;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTokenJti() {
        return tokenJti;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
