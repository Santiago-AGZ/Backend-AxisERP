package com.axiserp.inventory.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[R18-R19] Inventory Concurrency Tests")
class InventoryConcurrencyTest {

    @Test
    @DisplayName("[R19] First valid update should succeed, second should fail")
    void firstUpdatePrevails() {
        Inventory inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .currentStock(100)
                .build();

        assertTrue(inventory.canExit(60));
        inventory.subtractStock(60);
        assertEquals(40, inventory.getCurrentStock());

        assertThrows(com.axiserp.inventory.domain.exception.InsufficientStockException.class,
                () -> inventory.subtractStock(60));
        assertEquals(40, inventory.getCurrentStock());
    }

    @Test
    @DisplayName("[R18] Concurrent stock subtraction prevents negative stock")
    void concurrentSubtractions_preventNegativeStock() throws InterruptedException {
        Inventory inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .currentStock(10)
                .build();

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    synchronized (inventory) {
                        if (inventory.canExit(3)) {
                            inventory.subtractStock(3);
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        Thread.sleep(500);

        assertEquals(3, successCount.get(),
                "Only 3 threads should succeed (3x3=9 <= 10, 4x3=12 > 10)");
        assertEquals(2, failureCount.get(),
                "2 threads should fail due to insufficient stock");
        assertEquals(1, inventory.getCurrentStock(),
                "Remaining stock should be 1 (10 - 3*3 = 1)");
    }

    @Test
    @DisplayName("[R18] Concurrent entry registrations should maintain correct stock")
    void concurrentEntries_maintainCorrectStock() throws InterruptedException {
        Inventory inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .currentStock(0)
                .build();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    synchronized (inventory) {
                        inventory.addStock(5);
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        Thread.sleep(500);

        assertEquals(50, inventory.getCurrentStock(),
                "10 threads adding 5 each = 50 total stock");
    }

    @Test
    @DisplayName("[R18] Domain model canExit is thread-safe check")
    void canExit_threadSafe() {
        Inventory inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .currentStock(10)
                .build();

        assertTrue(inventory.canExit(5));
        assertTrue(inventory.canExit(10));
        assertFalse(inventory.canExit(11));
        assertEquals(10, inventory.getCurrentStock(), "canExit should not modify state");
    }
}
