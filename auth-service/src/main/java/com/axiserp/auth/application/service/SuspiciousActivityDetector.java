package com.axiserp.auth.application.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.AuditLog.AuditAction;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SuspiciousActivityDetector {

    private static final Logger log = LoggerFactory.getLogger(SuspiciousActivityDetector.class);

    private static final int SUSPICIOUS_THRESHOLD = 5;
    private static final long TIME_WINDOW_MS = 300_000;

    private final ConcurrentHashMap<String, FailedAttemptEntry> failedAttempts = new ConcurrentHashMap<>();
    private final AuditService auditService;

    public void recordFailedLogin(UUID userId, String email, String ipAddress, String userAgent) {
        String key = email != null ? email : userId.toString();
        long now = System.currentTimeMillis();

        FailedAttemptEntry entry = failedAttempts.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.timestamp) > TIME_WINDOW_MS) {
                return new FailedAttemptEntry(1, now, ipAddress, userId);
            }
            return new FailedAttemptEntry(existing.count + 1, now, ipAddress, userId);
        });

        if (entry.count >= SUSPICIOUS_THRESHOLD && !entry.ipAddress.equals(ipAddress)) {
            auditService.log(AuditAction.LOGIN, "AUTH", userId,
                    userId, email,
                    Map.of("suspicious", true, "reason", "Multiples IPs en ventana corta",
                            "attempts", entry.count, "ipAddress", ipAddress),
                    ipAddress, userAgent);
            log.warn("suspicious_activity_detected user_id={} email={} attempts={} ip={}",
                    userId, email, entry.count, ipAddress);
        }
    }

    private record FailedAttemptEntry(int count, long timestamp, String ipAddress, UUID userId) {}
}
