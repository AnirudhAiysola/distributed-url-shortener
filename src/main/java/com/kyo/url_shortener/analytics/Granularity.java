package com.kyo.url_shortener.analytics;

public enum Granularity {
    DAY("day"), WEEK("week"), MONTH("month");

    private final String sqlUnit;

    Granularity(String sqlUnit) {
        this.sqlUnit = sqlUnit;
    }

    public String sqlUnit() {
        return sqlUnit;
    }

    public static Granularity parse(String value) {
        for (Granularity g : values()) {
            if (g.name().equalsIgnoreCase(value)) {
                return g;
            }
        }
        throw new InvalidAnalyticsQueryException("granularity must be day, week, or month");
    }
}