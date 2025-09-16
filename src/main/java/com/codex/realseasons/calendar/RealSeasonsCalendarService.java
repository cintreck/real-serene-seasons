package com.codex.realseasons.calendar;

import com.codex.realseasons.config.RealSeasonsCadence;
import com.codex.realseasons.config.RealSeasonsCommonConfig;
import sereneseasons.api.season.Season;
import sereneseasons.season.SeasonTime;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Computes the desired Serene Seasons cycle position from the real-world calendar.
 */
public final class RealSeasonsCalendarService {
    private static final int SUB_SEASON_COUNT = Season.SubSeason.VALUES.length;
    private static final int MID_WINTER_INDEX = Season.SubSeason.MID_WINTER.ordinal();

    private final Clock clock;

    public RealSeasonsCalendarService(Clock clock) {
        this.clock = clock;
    }

    public RealSeasonsCalendarSnapshot snapshot(RealSeasonsCommonConfig config) {
        java.time.ZoneId zone = config.zoneId();
        LocalDateTime now = LocalDateTime.now(zone);
        java.time.temporal.WeekFields wf = java.time.temporal.WeekFields.of(config.weekStartDay(), config.weekMinDays());
        RealSeasonsCadence.Position pos = config.cadence().position(now, config.interpolateTimeOfDay(), wf);
        SeasonTime seasonTime = computeSeasonTime(pos, config);
        return new RealSeasonsCalendarSnapshot(seasonTime, pos);
    }

    private SeasonTime computeSeasonTime(RealSeasonsCadence.Position position, RealSeasonsCommonConfig config) {
        int ticksPerSubSeason = SeasonTime.ZERO.getSubSeasonDuration();
        int cycleDuration = SeasonTime.ZERO.getCycleDuration();

        int completed = position.completedSubSeasons();
        double fraction = position.fractionInSubSeason();

        int resolvedIndex = Math.floorMod(MID_WINTER_INDEX + completed, SUB_SEASON_COUNT);
        long tickOffset = (long) resolvedIndex * ticksPerSubSeason;
        long fractionalTicks = (long) Math.floor(fraction * ticksPerSubSeason);

        int targetTicks = Math.floorMod((int) (tickOffset + fractionalTicks), cycleDuration);

        if (config.useSouthernHemisphere()) {
            targetTicks = Math.floorMod(targetTicks + cycleDuration / 2, cycleDuration);
        }

        return new SeasonTime(targetTicks);
    }

    public record RealSeasonsCalendarSnapshot(SeasonTime seasonTime, RealSeasonsCadence.Position position) {
    }
}
