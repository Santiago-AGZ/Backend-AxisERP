package com.axiserp.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.axiserp.auth.domain.service.FailedAttemptRateLimitStrategy;
import com.axiserp.auth.domain.service.LoginRateLimitStrategy;

@Configuration
public class DomainBeansConfig {

    @Bean
    public LoginRateLimitStrategy loginRateLimitStrategy() {
        return new FailedAttemptRateLimitStrategy();
    }
}
