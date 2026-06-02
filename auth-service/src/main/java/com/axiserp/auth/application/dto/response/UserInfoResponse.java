package com.axiserp.auth.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserInfoResponse {

    private UUID id;
    private String name;
    private String email;
    private String role;
    private String status;
    private LocalDateTime lastLoginAt;
}
