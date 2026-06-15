package com.axiserp.inventory.application.service;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;

public final class OptimisticRetryUtil {

    private static final Logger log = LoggerFactory.getLogger(OptimisticRetryUtil.class);
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 50;

    private OptimisticRetryUtil() {}

    public static <T> T retry(Supplier<T> operation) {
        int attempt = 0;
        while (true) {
            try {
                return operation.get();
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    log.error("optimistic_retry_exhausted attempts={}", attempt, e);
                    throw e;
                }
                long delay = BASE_DELAY_MS * (1L << attempt);
                log.warn("optimistic_lock_conflict attempt={}/{} retrying after {}ms",
                        attempt, MAX_RETRIES, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
    }
}
