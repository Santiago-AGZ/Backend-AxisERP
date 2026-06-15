package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.axiserp.auth.application.service.JwtService;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import jakarta.servlet.http.HttpServletResponse;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityTestConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh",
                    "/api/v1/auth/password-reset", "/api/v1/auth/password-reset/confirm")
                .permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                })
            );
        return http.build();
    }

    @Bean
    public JwtService jwtService() {
        return mock(JwtService.class);
    }

    @Bean
    public TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort() {
        return mock(TokenBlacklistRepositoryPort.class);
    }

    @Bean
    public UserRepositoryPort userRepositoryPort() {
        UserRepositoryPort mock = mock(UserRepositoryPort.class);
        User activeUser = User.builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            .name("Test")
            .email("test@test.com")
            .status(User.UserStatus.ACTIVO)
            .build();
        when(mock.findById(any())).thenReturn(Optional.of(activeUser));
        when(mock.save(any())).thenReturn(activeUser);
        return mock;
    }

    @Bean
    public RoleRepositoryPort roleRepositoryPort() {
        return mock(RoleRepositoryPort.class);
    }

    @Bean
    public TokenBlacklistService tokenBlacklistService() {
        return mock(TokenBlacklistService.class);
    }
}
