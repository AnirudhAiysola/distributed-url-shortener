package com.kyo.url_shortener.analytics;

import io.awspring.cloud.sqs.operations.SqsAsyncOperations;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Slf4j
@Component
public class ClickEventPublisher {

    private final SqsAsyncOperations sqs;
    private final Clock clock;
    private final String queue;
    private final MeterRegistry meterRegistry;

    public ClickEventPublisher(SqsAsyncOperations sqs,
                               Clock clock,
                               @Value("${shortener.analytics.queue}") String queue, MeterRegistry meterRegistry) {
        this.sqs = sqs;
        this.clock = clock;
        this.queue = queue;
        this.meterRegistry = meterRegistry;
    }

    // Fire-and-forget: must NEVER throw or block the redirect
    public void publish(String code) {
        ClickEvent clickEvent = new ClickEvent(UUID.randomUUID(), code, clock.instant());
        try {
            sqs.sendAsync(queue,clickEvent)
                    .whenComplete((result, ex) -> {
                        if(ex != null){
                            log.warn("Failed to publish click event for {}: {}", code, ex.getMessage());
                        }
                        meterRegistry.counter("shortener.clicks.published").increment();
                    });
        } catch (RuntimeException ex) {
            log.warn(ex.getMessage());
        }
    }
}