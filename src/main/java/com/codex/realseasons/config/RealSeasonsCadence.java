package com.codex.realseasons.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Defines how long a single sub-season lasts in real time.
 *
 * Each cadence returns a position made of:
 *  - completedSubSeasons: how many whole sub-seasons have elapsed since the anchor (Mid Winter)
 *  - fractionInSubSeason: 0..1 progress inside the current sub-season
 */
public enum RealSeasonsCadence {
    MONTH("One sub-season per real calendar month") {
        @Override
        public Position position(LocalDateTime now, boolean interpolate) {
            int monthIndex = now.getMonthValue() - 1; // 0..11
            // Mid Winter is January, then Late Winter (Feb), then Early Spring (Mar), etc.
            int completed = monthIndex; // Each month advances exactly 1 sub-season
            double fraction = interpolate ? fractionOfMonth(now.toLocalDate(), now.toLocalTime()) : 0.0D;
            return new Position(completed, fraction);
        }
    },
    WEEK("One sub-season per real week (ISO weeks)") {
        @Override
        public Position position(LocalDateTime now, boolean interpolate, WeekFields wf) {
            int weekIndex = Math.max(1, now.get(wf.weekOfYear())); // 1..52/53
            int completed = (weekIndex - 1) % SUB_SEASON_COUNT;
            double fraction = interpolate ? (now.getDayOfWeek().getValue() - 1 + fractionOfDay(now.toLocalTime())) / 7.0D : 0.0D;
            return new Position(completed, clamp(fraction));
        }
    },
    DAY("One sub-season per real day") {
        @Override
        public Position position(LocalDateTime now, boolean interpolate) {
            int dayIndex = (now.getDayOfYear() - 1) % SUB_SEASON_COUNT; // 0..11
            double fraction = interpolate ? fractionOfDay(now.toLocalTime()) : 0.0D;
            return new Position(dayIndex, fraction);
        }
    };

    public record Position(int completedSubSeasons, double fractionInSubSeason) {}

    private static final int SUB_SEASON_COUNT = 12;
    private final String description;

    RealSeasonsCadence(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public String fileKey() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Position position(LocalDateTime now, boolean interpolate) {
        return position(now, interpolate, WeekFields.ISO);
    }

    // Optional WeekFields-aware variant; defaults to the simple one unless overridden
    public Position position(LocalDateTime now, boolean interpolate, WeekFields wf) {
        return position(now, interpolate);
    }

    public static RealSeasonsCadence fromString(Object raw, RealSeasonsCadence fallback) {
        if (raw instanceof String text) {
            String key = text.trim().toUpperCase(Locale.ROOT);
            // Backward-compatible aliases
            if ("REAL_YEAR".equals(key)) key = "MONTH"; // previous semantics mapped to months
            if ("REAL_WEEK".equals(key)) key = "WEEK";
            if ("REAL_DAY".equals(key)) key = "DAY";
            try {
                return RealSeasonsCadence.valueOf(key);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return fallback;
    }

    private static double fractionOfDay(LocalTime time) {
        return time.toSecondOfDay() / 86400.0D;
    }

    private static double fractionOfMonth(LocalDate date, LocalTime time) {
        int length = date.lengthOfMonth();
        int dayIndex = date.getDayOfMonth() - 1;
        return (dayIndex + fractionOfDay(time)) / (double) length;
    }

    private static double clamp(double value) {
        double wrapped = value % 1.0D;
        return wrapped < 0 ? wrapped + 1.0D : wrapped;
    }
}
