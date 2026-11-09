package com.kyo.url_shortener.analytics;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "shortener.worker.enabled", havingValue = "true", matchIfMissing = true)
public class ClickEventListener {

    private final ClickEventProcessor processor;

    @SqsListener("${shortener.analytics.queue}")
    public void onClicks(List<ClickEvent> events) {
        processor.process(events);
        log.debug("Processed {} click events", events.size());
    }
}