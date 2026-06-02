package com.axiserp.auth.infrastructure.adapters.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "token_type", nullable = false, length = 20)
    private String tokenType;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blacklistedAt;

    @PrePersist
    protected void onCreate() {
        this.blacklistedAt = LocalDateTime.now();
        if (this.tokenType == null) {
            this.tokenType = "refresh";
        }
        if (this.reason == null) {
            this.reason = "REVOKED";
        }
    }
}
