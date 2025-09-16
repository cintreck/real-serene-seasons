package com.codex.realseasons;

import com.codex.realseasons.config.RealSeasonsCadence;
import com.codex.realseasons.config.RealSeasonsCommonConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Builds the Cloth Config screen for Real Serene Seasons when available.
 */
public final class RealSeasonsClientConfigScreenFactory {
    private RealSeasonsClientConfigScreenFactory() {
    }

    public static Screen create(Screen parent) {
        RealSeasonsCommonConfig current = RealSeasonsCommonConfig.loadOrCreate();
        final boolean[] useSouthernHemisphere = { current.useSouthernHemisphere() };
        final RealSeasonsCadence[] cadence = { current.cadence() };
        final boolean[] interpolateTimeOfDay = { current.interpolateTimeOfDay() };
        final String[] timezoneId = { current.timezoneId() };
        final java.time.DayOfWeek[] weekStart = { current.weekStartDay() };
        final int[] weekMinDays = { current.weekMinDays() };

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Real Serene Seasons"));
        builder.setSavingRunnable(() -> {
            RealSeasonsCommonConfig updated = RealSeasonsCommonConfig.overwrite(
                    useSouthernHemisphere[0],
                    cadence[0],
                    interpolateTimeOfDay[0],
                    timezoneId[0],
                    weekStart[0],
                    weekMinDays[0]
            );
            RealSeasonsModInitializer.reloadConfig(updated);
        });

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("Calendar"));
        ConfigEntryBuilder entries = builder.entryBuilder();

        general.addEntry(entries.startBooleanToggle(Component.literal("Use Southern Hemisphere"), useSouthernHemisphere[0])
                .setDefaultValue(false)
                .setTooltip(Component.literal("Flips season order so January becomes mid summer instead of mid winter."))
                .setSaveConsumer(value -> useSouthernHemisphere[0] = value)
                .build());

        general.addEntry(entries.startEnumSelector(Component.literal("Cadence (per sub-season)"), RealSeasonsCadence.class, cadence[0])
                .setDefaultValue(RealSeasonsCadence.MONTH)
                .setTooltip(Component.literal("How long one sub-season lasts in real time: Month, Week, or Day. The full cycle is always 12 sub-seasons."))
                .setSaveConsumer(value -> cadence[0] = value)
                .build());

        general.addEntry(entries.startBooleanToggle(Component.literal("Interpolate Time Of Day"), interpolateTimeOfDay[0])
                .setDefaultValue(true)
                .setTooltip(Component.literal("If enabled, transitions happen smoothly throughout each real day."))
                .setSaveConsumer(value -> interpolateTimeOfDay[0] = value)
                .build());

        general.addEntry(entries.startStrField(Component.literal("Timezone ID"), timezoneId[0])
                .setDefaultValue("")
                .setTooltip(Component.literal("IANA ZoneId (e.g., America/New_York, Europe/Berlin, UTC). Blank = system default."))
                .setSaveConsumer(value -> timezoneId[0] = value)
                .build());

        general.addEntry(entries.startEnumSelector(Component.literal("Week Start Day"), java.time.DayOfWeek.class, weekStart[0])
                .setDefaultValue(java.time.DayOfWeek.MONDAY)
                .setTooltip(Component.literal("Used when cadence=WEEK. First day of week."))
                .setSaveConsumer(value -> weekStart[0] = value)
                .build());

        general.addEntry(entries.startIntField(Component.literal("Week Min Days (first week)"), weekMinDays[0])
                .setDefaultValue(4)
                .setMin(1)
                .setMax(7)
                .setTooltip(Component.literal("Used when cadence=WEEK. Minimum days in first ISO week (1-7)."))
                .setSaveConsumer(value -> weekMinDays[0] = Math.max(1, Math.min(7, value)))
                .build());

        return builder.build();
    }
}
