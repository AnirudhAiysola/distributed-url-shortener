package com.kyo.url_shortener.analytics;

import io.awspring.cloud.sqs.operations.SqsAsyncOperations;
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

    public ClickEventPublisher(SqsAsyncOperations sqs,
                               Clock clock,
                               @Value("${shortener.analytics.queue}") String queue) {
        this.sqs = sqs;
        this.clock = clock;
        this.queue = queue;
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
                    });
        } catch (RuntimeException ex) {
            log.warn(ex.getMessage());
        }
    }
}