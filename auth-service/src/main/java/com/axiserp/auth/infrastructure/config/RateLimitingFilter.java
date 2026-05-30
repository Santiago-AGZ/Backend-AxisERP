package com.axiserp.auth.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> ipBuckets = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        Bucket bucket = ipBuckets.get(clientIp, k -> createBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 429);
            body.put("error", "Too Many Requests");
            body.put("message", "Demasiadas solicitudes. Intente nuevamente en 60 segundos.");
            body.put("timestamp", java.time.Instant.now().toString());

            try (var writer = response.getWriter()) {
                new com.fasterxml.jackson.databind.ObjectMapper().writeValue(writer, body);
            }
        }
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
