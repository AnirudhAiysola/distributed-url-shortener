package com.kyo.url_shortener.analytics;

import io.awspring.cloud.sqs.operations.SqsAsyncOperations;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClickEventPublisherTest {

    private static final Instant NOW = Instant.parse("2026-11-09T19:30:00Z");

    private final SqsAsyncOperations sqs = mock(SqsAsyncOperations.class);
    private final ClickEventPublisher publisher =
            new ClickEventPublisher(sqs, Clock.fixed(NOW, ZoneOffset.UTC), "click-events");

    @Test
    void sendsAnEventWithCodeTimeAndId() {
        when(sqs.sendAsync(eq("click-events"), any())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.publish("abc1234");

        ArgumentCaptor<ClickEvent> captor = ArgumentCaptor.forClass(ClickEvent.class);
        verify(sqs).sendAsync(eq("click-events"), captor.capture());
        assertThat(captor.getValue().code()).isEqualTo("abc1234");
        assertThat(captor.getValue().clickedAt()).isEqualTo(NOW);
        assertThat(captor.getValue().eventId()).isNotNull();
    }

    @Test
    void immediateFailureDoesNotThrow() {
        when(sqs.sendAsync(eq("click-events"), any())).thenThrow(new RuntimeException("sqs down"));

        assertThatCode(() -> publisher.publish("abc1234")).doesNotThrowAnyException();
    }

    @Test
    void laterFailureDoesNotThrow() {
        when(sqs.sendAsync(eq("click-events"), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("sqs down")));

        assertThatCode(() -> publisher.publish("abc1234")).doesNotThrowAnyException();
    }
}