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
public class PasswordHistory {

    private UUID id;
    private UUID userId;
    private String passwordHash;
    private LocalDateTime createdAt;
}
