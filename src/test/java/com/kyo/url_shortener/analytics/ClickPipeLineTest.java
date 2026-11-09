package com.kyo.url_shortener.analytics;

import com.kyo.url_shortener.TestcontainersConfiguration;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ClickPipelineTest {

    @Autowired
    SqsTemplate sqsTemplate;

    @Autowired
    ClickEventProcessor processor;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void duplicateEventsThroughSqsAreCountedOnce() {
        Instant time = Instant.parse("2026-11-09T19:30:00Z");
        ClickEvent first = new ClickEvent(UUID.randomUUID(), "pipe001", time);
        ClickEvent second = new ClickEvent(UUID.randomUUID(), "pipe001", time);

        sqsTemplate.send("click-events", first);
        sqsTemplate.send("click-events", first);   // simulated redelivery
        sqsTemplate.send("click-events", second);

        // Wait until the worker has counted them, then check it STAYS at 2 (no late double count)
        await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> assertThat(dailyClicks("pipe001", LocalDate.of(2026, 11, 9))).isEqualTo(2));
        await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> assertThat(dailyClicks("pipe001", LocalDate.of(2026, 11, 9))).isEqualTo(2));
    }

    @Test
    void clicksAreBucketedByUtcDay() {
        processor.process(List.of(
                new ClickEvent(UUID.randomUUID(), "pipe002", Instant.parse("2026-11-09T23:59:00Z")),
                new ClickEvent(UUID.randomUUID(), "pipe002", Instant.parse("2026-11-10T00:01:00Z"))));

        assertThat(dailyClicks("pipe002", LocalDate.of(2026, 11, 9))).isEqualTo(1);
        assertThat(dailyClicks("pipe002", LocalDate.of(2026, 11, 10))).isEqualTo(1);
    }

    private long dailyClicks(String code, LocalDate day) {
        List<Long> rows = jdbc.queryForList(
                "SELECT clicks FROM daily_clicks WHERE code = ? AND day = ?", Long.class, code, day);
        return rows.isEmpty() ? 0 : rows.getFirst();
    }
}