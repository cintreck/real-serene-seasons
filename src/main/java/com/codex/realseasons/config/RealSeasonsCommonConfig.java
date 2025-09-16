package com.codex.realseasons.config;

import com.codex.realseasons.RealSeasonsSharedData;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and persists configuration used to map the real-world calendar to Serene Seasons.
 */
public final class RealSeasonsCommonConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("real-seasons-config");
    private static final String FILE_NAME = "real_serene_seasons.toml";

    private final boolean useSouthernHemisphere;
    private final RealSeasonsCadence cadence;
    private final boolean interpolateTimeOfDay;
    private final String timezoneId; // IANA ZoneId or blank for system default
    private final java.time.DayOfWeek weekStartDay;
    private final int weekMinDays;

    private RealSeasonsCommonConfig(boolean useSouthernHemisphere, RealSeasonsCadence cadence, boolean interpolateTimeOfDay,
                                    String timezoneId, java.time.DayOfWeek weekStartDay, int weekMinDays) {
        this.useSouthernHemisphere = useSouthernHemisphere;
        this.cadence = cadence;
        this.interpolateTimeOfDay = interpolateTimeOfDay;
        this.timezoneId = timezoneId;
        this.weekStartDay = weekStartDay;
        this.weekMinDays = Math.max(1, Math.min(7, weekMinDays));
    }

    public boolean useSouthernHemisphere() {
        return useSouthernHemisphere;
    }

    public RealSeasonsCadence cadence() {
        return cadence;
    }

    public boolean interpolateTimeOfDay() {
        return interpolateTimeOfDay;
    }

    public String timezoneId() {
        return timezoneId;
    }

    public java.time.ZoneId zoneId() {
        try {
            if (timezoneId != null && !timezoneId.isBlank()) {
                return java.time.ZoneId.of(timezoneId);
            }
        } catch (Throwable ignored) {}
        return java.time.ZoneId.systemDefault();
    }

    public java.time.DayOfWeek weekStartDay() {
        return weekStartDay;
    }

    public int weekMinDays() {
        return weekMinDays;
    }

    public static RealSeasonsCommonConfig loadOrCreate() {
        RealSeasonsCommonConfig defaults = new RealSeasonsCommonConfig(false, RealSeasonsCadence.MONTH, true,
                "", java.time.DayOfWeek.MONDAY, 4);
        Path path = configPath();
        if (!Files.exists(path)) {
            writeConfig(path, defaults);
            return defaults;
        }

        try (CommentedFileConfig file = openFile(path)) {
            file.load();
            RealSeasonsCommonConfig loaded = fromToml(file, defaults);
            writeToFile(file, loaded);
            return loaded;
        } catch (Throwable t) {
            LOGGER.error("Failed to load {}: {}", FILE_NAME, t.toString(), t);
            writeConfig(path, defaults);
            return defaults;
        }
    }

    public static RealSeasonsCommonConfig overwrite(boolean useSouthernHemisphere, RealSeasonsCadence cadence, boolean interpolateTimeOfDay,
                                                    String timezoneId, java.time.DayOfWeek weekStartDay, int weekMinDays) {
        RealSeasonsCommonConfig updated = new RealSeasonsCommonConfig(useSouthernHemisphere, cadence, interpolateTimeOfDay,
                timezoneId, weekStartDay, weekMinDays);
        writeConfig(configPath(), updated);
        return updated;
    }

    private static RealSeasonsCommonConfig fromToml(CommentedConfig config, RealSeasonsCommonConfig defaults) {
        boolean useSouthern = defaults.useSouthernHemisphere;
        Object rawSetting = config.get("use_southern_hemisphere");
        if (rawSetting instanceof Boolean bool) {
            useSouthern = bool;
        }

        RealSeasonsCadence cadence = defaults.cadence;
        Object rawCadence = config.get("cadence");
        cadence = RealSeasonsCadence.fromString(rawCadence, cadence);

        boolean interpolate = defaults.interpolateTimeOfDay;
        Object rawInterpolation = config.get("interpolate_time_of_day");
        if (rawInterpolation instanceof Boolean bool) {
            interpolate = bool;
        }

        String tz = defaults.timezoneId;
        Object rawTz = config.get("timezone_id");
        if (rawTz instanceof String s) {
            tz = s.trim();
        }

        java.time.DayOfWeek weekStart = defaults.weekStartDay;
        Object rawStart = config.get("week_start_day");
        if (rawStart instanceof String s) {
            try { weekStart = java.time.DayOfWeek.valueOf(s.trim().toUpperCase(java.util.Locale.ROOT)); } catch (IllegalArgumentException ignored) {}
        }

        int minDays = defaults.weekMinDays;
        Object rawMin = config.get("week_min_days");
        if (rawMin instanceof Number n) {
            minDays = Math.max(1, Math.min(7, n.intValue()));
        }

        return new RealSeasonsCommonConfig(useSouthern, cadence, interpolate, tz, weekStart, minDays);
    }

    private static void writeConfig(Path path, RealSeasonsCommonConfig config) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory for {}", FILE_NAME, e);
            return;
        }

        try (CommentedFileConfig file = openFile(path)) {
            file.load();
            writeToFile(file, config);
        } catch (Throwable t) {
            LOGGER.error("Failed to write {}: {}", FILE_NAME, t.toString(), t);
        }
    }

    private static CommentedFileConfig openFile(Path path) {
        return CommentedFileConfig.builder(path)
                .preserveInsertionOrder()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
    }

    private static void writeToFile(CommentedFileConfig file, RealSeasonsCommonConfig config) {
        file.set("#", "Real Serene Seasons - Config");
        file.set("#1", "Controls how calendar-based season syncing is interpreted.");

        file.set("use_southern_hemisphere", config.useSouthernHemisphere);
        file.setComment("use_southern_hemisphere", "If true, invert the calendar to match southern hemisphere seasons.");

        file.set("cadence", config.cadence.name());
        file.setComment("cadence", "Per-sub-season cadence: MONTH (calendar month), WEEK (7 days), or DAY (24 hours). Full cycle = 12 sub-seasons.");

        file.set("interpolate_time_of_day", config.interpolateTimeOfDay);
        file.setComment("interpolate_time_of_day", "If true, include the current time of day when mapping the calendar (smoother transitions).");

        file.set("timezone_id", config.timezoneId);
        file.setComment("timezone_id", "IANA ZoneId (e.g., America/New_York, Europe/Berlin, UTC). Blank = system default.");

        file.set("week_start_day", config.weekStartDay.name());
        file.setComment("week_start_day", "Only used when cadence=WEEK. First day of week: MONDAY/TUESDAY/.../SUNDAY.");

        file.set("week_min_days", config.weekMinDays);
        file.setComment("week_min_days", "Only used when cadence=WEEK. Minimum days in first week (1-7). ISO uses 4.");

        file.save();
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(RealSeasonsSharedData.MOD_ID + ".toml");
    }
}
