package com.kyo.url_shortener.analytics;

import java.time.LocalDate;

public record PeriodClicks(LocalDate period, long clicks) {
}