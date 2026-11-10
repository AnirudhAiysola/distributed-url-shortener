package com.kyo.url_shortener.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnalyticsRepository {

    private final JdbcTemplate jdbc;

    // Returns up to `limit` periods strictly AFTER `after`, oldest first
    public List<PeriodClicks> findPeriods(String code, LocalDate from, LocalDate to,
                                          Granularity granularity, LocalDate after, int limit) {
        String unit = granularity.sqlUnit();   // from our enum, never raw user input
        String sql = """
                SELECT date_trunc('%s', day)::date AS period, SUM(clicks) AS clicks
                FROM daily_clicks
                WHERE code = ? AND day BETWEEN ? AND ?
                  AND date_trunc('%s', day)::date > ?
                GROUP BY period
                ORDER BY period
                LIMIT ?
                """.formatted(unit, unit);

        return jdbc.query(sql,
                (rs, rowNum) -> new PeriodClicks(rs.getObject("period", LocalDate.class), rs.getLong("clicks")),
                code, from, to, after, limit);
    }

    public long totalClicks(String code, LocalDate from, LocalDate to) {
        Long total = jdbc.queryForObject(
                "SELECT COALESCE(SUM(clicks), 0) FROM daily_clicks WHERE code = ? AND day BETWEEN ? AND ?",
                Long.class, code, from, to);
        return total == null ? 0 : total;
    }
}