-- Raw click events, partitioned by month
CREATE TABLE click_events (
                              event_id   UUID        NOT NULL,
                              code       VARCHAR(32) NOT NULL,
                              clicked_at TIMESTAMPTZ NOT NULL,
                              PRIMARY KEY (event_id, clicked_at)   -- must include the partition column
) PARTITION BY RANGE (clicked_at);

CREATE TABLE click_events_2026_11 PARTITION OF click_events
    FOR VALUES FROM ('2026-11-01 00:00:00+00') TO ('2026-12-01 00:00:00+00');

CREATE TABLE click_events_2026_12 PARTITION OF click_events
    FOR VALUES FROM ('2026-12-01 00:00:00+00') TO ('2027-01-01 00:00:00+00');

-- Catches anything outside the ranges above, so inserts never fail
CREATE TABLE click_events_default PARTITION OF click_events DEFAULT;

-- Pre-aggregated counts: what the analytics API reads
CREATE TABLE daily_clicks (
                              code   VARCHAR(32) NOT NULL,
                              day    DATE        NOT NULL,
                              clicks BIGINT      NOT NULL,
                              PRIMARY KEY (code, day)
);