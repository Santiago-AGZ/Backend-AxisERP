package com.axiserp.auth.domain.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String token;
    private TokenStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime revokedAt;
    private boolean revoked;
    private String ipAddress;
    private String userAgent;

    public RefreshToken(UUID id, UUID userId, String token, LocalDateTime expiresAt, LocalDateTime createdAt, boolean revoked) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.revoked = revoked;
        this.status = revoked ? TokenStatus.REVOKED : TokenStatus.ACTIVE;
    }

    public RefreshToken(UUID userId, String token, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
        this.revoked = false;
        this.status = TokenStatus.ACTIVE;
    }

    public void revoke() {
        this.revoked = true;
        this.status = TokenStatus.REVOKED;
        this.revokedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public boolean isExpired() {
        return LocalDateTime.now(ZoneOffset.UTC).isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public enum TokenStatus {
        ACTIVE, REVOKED, EXPIRED
    }
}
