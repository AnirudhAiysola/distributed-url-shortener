package com.kyo.url_shortener.analytics;

import com.kyo.url_shortener.url.ShortUrlNotFoundException;
import com.kyo.url_shortener.url.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    static final int DEFAULT_LIMIT = 100;
    static final int MAX_LIMIT = 100;
    static final int DEFAULT_RANGE_DAYS = 30;
    private static final LocalDate FIRST_PAGE = LocalDate.of(1970, 1, 1);

    private final AnalyticsRepository repository;
    private final ShortUrlRepository urls;
    private final Clock clock;

    public AnalyticsResponse analytics(String code, LocalDate from, LocalDate to,
                                       String granularity, String cursor, Integer limit) {
        // 1. The link must exist
        if (!urls.existsById(code)) {
            throw new ShortUrlNotFoundException(code);
        }

        // 2. Date range, with defaults
        LocalDate end = (to != null) ? to : LocalDate.now(clock);
        LocalDate start = (from != null) ? from : end.minusDays(DEFAULT_RANGE_DAYS - 1);
        if (start.isAfter(end)) {
            throw new InvalidAnalyticsQueryException("from must be on or before to");
        }

        // 3. Granularity
        Granularity g = Granularity.parse(granularity == null ? "day" : granularity);

        // 4. Page size
        int pageSize = (limit == null) ? DEFAULT_LIMIT : limit;
        if (pageSize < 1 || pageSize > MAX_LIMIT) {
            throw new InvalidAnalyticsQueryException("limit must be between 1 and " + MAX_LIMIT);
        }

        // 5. Where this page starts
        LocalDate after = (cursor == null) ? FIRST_PAGE : decodeCursor(cursor);

        // 6. Fetch one extra row to know if there's a next page
        List<PeriodClicks> rows = repository.findPeriods(code, start, end, g, after, pageSize + 1);
        boolean hasMore = rows.size() > pageSize;
        List<PeriodClicks> page = hasMore ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasMore ? encodeCursor(page.getLast().period()) : null;

        // 7. Total for the whole range (not just this page)
        long total = repository.totalClicks(code, start, end);

        return new AnalyticsResponse(code, start, end, g.name().toLowerCase(), total, page, nextCursor);
    }

    private String encodeCursor(LocalDate period) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(period.toString().getBytes(StandardCharsets.UTF_8));
    }

    private LocalDate decodeCursor(String cursor) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor);
            return LocalDate.parse(new String(bytes, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            throw new InvalidAnalyticsQueryException("invalid cursor");
        }
    }
}