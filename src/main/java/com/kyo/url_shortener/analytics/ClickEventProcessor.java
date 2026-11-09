package com.kyo.url_shortener.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClickEventProcessor {

    private static final String INSERT_EVENT = """
            INSERT INTO click_events (event_id, code, clicked_at)
            VALUES (?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String UPSERT_DAILY = """
            INSERT INTO daily_clicks (code, day, clicks)
            VALUES (?, ?, ?)
            ON CONFLICT (code, day) DO UPDATE SET clicks = daily_clicks.clicks + EXCLUDED.clicks
            """;

    private final JdbcTemplate jdbc;

    @Transactional
    public void process(List<ClickEvent> events) {
        Map<DailyKey, Long> newClicks = new HashMap<>();

        for (ClickEvent event : events) {
            int inserted = jdbc.update(INSERT_EVENT, event.eventId(), event.code(), Timestamp.from(event.clickedAt()));

            if(inserted == 1){
                LocalDate day = LocalDate.ofInstant(event.clickedAt(), ZoneOffset.UTC);
                newClicks.merge(new DailyKey(event.code() ,day),1L,Long::sum);
            }
        }

        newClicks.forEach((key, count) -> jdbc.update(UPSERT_DAILY, key.code,key.day,count));
    }

    private record DailyKey(String code, LocalDate day) {
    }
}