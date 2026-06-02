package com.axiserp.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {
    private UUID id;
    private String token;
    private String tokenJti;
    private UUID userId;
    private String tokenType;
    private String reason;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public TokenBlacklist(UUID id, String tokenJti, UUID userId, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.tokenJti = tokenJti;
        this.token = tokenJti;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public TokenBlacklist(String tokenJti, UUID userId, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.tokenJti = tokenJti;
        this.token = tokenJti;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
