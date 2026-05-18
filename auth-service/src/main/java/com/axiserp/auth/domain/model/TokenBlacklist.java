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
    private String tokenType;
    private UUID userId;
    private String reason;
    private LocalDateTime expiresAt;
    private LocalDateTime blacklistedAt;
}
