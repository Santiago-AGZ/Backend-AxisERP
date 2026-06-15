package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.axiserp.auth.application.service.JwtService;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import jakarta.servlet.http.HttpServletResponse;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public JwtService jwtService() {
        return mock(JwtService.class);
    }

    @Bean
    public TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort() {
        return mock(TokenBlacklistRepositoryPort.class);
    }

    @Bean
    @Order(0)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
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
    public UserRepositoryPort userRepositoryPort() {
        return mock(UserRepositoryPort.class);
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
