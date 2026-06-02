package com.axiserp.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class OtpToken {
    private UUID id;
    private UUID userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean used;

    public OtpToken(UUID id, UUID userId, String token, LocalDateTime expiresAt, LocalDateTime createdAt, boolean used) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.used = used;
    }

    public OtpToken(UUID userId, String token, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.used = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void markAsUsed() {
        this.used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
