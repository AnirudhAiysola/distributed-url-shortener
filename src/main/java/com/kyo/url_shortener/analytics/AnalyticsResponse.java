package com.kyo.url_shortener.analytics;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsResponse(
        String code,
        LocalDate from,
        LocalDate to,
        String granularity,
        long totalClicks,
        List<PeriodClicks> data,
        String nextCursor
) {
}