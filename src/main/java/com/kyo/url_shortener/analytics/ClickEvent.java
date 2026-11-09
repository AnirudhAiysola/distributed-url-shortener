package com.kyo.url_shortener.analytics;

import java.time.Instant;
import java.util.UUID;

public record ClickEvent(UUID eventId, String code, Instant clickedAt) {
}