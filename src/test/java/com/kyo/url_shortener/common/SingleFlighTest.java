package com.kyo.url_shortener.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SingleFlightTest {

    private final SingleFlight<String> singleFlight = new SingleFlight<>();

    @Test
    void concurrentCallsForSameKeyLoadOnlyOnce() throws Exception {
        AtomicInteger loads = new AtomicInteger();
        List<String> results = new CopyOnWriteArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> results.add(singleFlight.execute("hot", () -> {
                    loads.incrementAndGet();
                    sleep(200); // a slow DB query, so all 100 calls overlap
                    return "https://example.com";
                })));
            }
        }

        assertThat(loads.get()).isEqualTo(1);
        assertThat(results).hasSize(100).containsOnly("https://example.com");
    }

    @Test
    void followersReceiveTheLeadersException() throws Exception {
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 20; i++) {
                executor.submit(() -> {
                    try {
                        singleFlight.execute("missing", () -> {
                            sleep(200);
                            throw new IllegalStateException("not found");
                        });
                    } catch (IllegalStateException ex) {
                        errors.add(ex);
                    }
                });
            }
        }

        // Every caller got the ORIGINAL exception type, not a wrapped one
        assertThat(errors).hasSize(20);
    }

    @Test
    void differentKeysLoadIndependently() {
        AtomicInteger loads = new AtomicInteger();

        singleFlight.execute("a", () -> { loads.incrementAndGet(); return "1"; });
        singleFlight.execute("b", () -> { loads.incrementAndGet(); return "2"; });

        assertThat(loads.get()).isEqualTo(2);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}