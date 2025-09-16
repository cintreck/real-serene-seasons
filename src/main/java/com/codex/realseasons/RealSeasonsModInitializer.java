package com.codex.realseasons;

import com.codex.realseasons.calendar.RealSeasonsCalendarService;
import com.codex.realseasons.config.RealSeasonsCommonConfig;
import com.codex.realseasons.network.RealSeasonsPackets;
import com.codex.realseasons.server.RealSeasonsSeasonDataProvider;
import com.codex.realseasons.server.RealSeasonsSeasonStateStore;
import com.codex.realseasons.server.RealSeasonsSeasonSynchronizer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import sereneseasons.api.season.SeasonHelper;

import java.time.Clock;
import java.util.Objects;

/**
 * Entry point for Real Serene Seasons.
 */
public final class RealSeasonsModInitializer implements ModInitializer {
    private static volatile RealSeasonsCommonConfig config;
    private static RealSeasonsSeasonSynchronizer synchronizer;
    private static RealSeasonsSeasonStateStore stateStore;

    @Override
    public void onInitialize() {
        config = RealSeasonsCommonConfig.loadOrCreate();
        stateStore = new RealSeasonsSeasonStateStore();
        RealSeasonsCalendarService calendarService = new RealSeasonsCalendarService(Clock.systemDefaultZone());
        synchronizer = new RealSeasonsSeasonSynchronizer(() -> config, calendarService, stateStore);
        synchronizer.register();

        // Register S2C payload codec on the server only. The client registers in its own initializer.
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            PayloadTypeRegistry.playS2C().register(RealSeasonsPackets.DisplayDaysPayload.TYPE, RealSeasonsPackets.DisplayDaysPayload.STREAM_CODEC);
        }

        SeasonHelper.ISeasonDataProvider originalProvider = SeasonHelper.dataProvider;
        if (originalProvider != null) {
            SeasonHelper.dataProvider = new RealSeasonsSeasonDataProvider(stateStore, originalProvider);
        } else {
            RealSeasonsSharedData.LOGGER.warn("SeasonHelper data provider was null during initialization; real-time sync relies on it to propagate to clients.");
        }

        RealSeasonsSharedData.LOGGER.info(
                "Real Serene Seasons ready: hemisphere {}, cadence {}, interpolation {}.",
                config.useSouthernHemisphere() ? "southern" : "northern",
                config.cadence(),
                config.interpolateTimeOfDay() ? "enabled" : "disabled"
        );
    }

    public static RealSeasonsCommonConfig config() {
        return config;
    }

    public static void reloadConfig(RealSeasonsCommonConfig newConfig) {
        Objects.requireNonNull(newConfig, "newConfig");
        config = newConfig;
        if (synchronizer != null) {
            synchronizer.resetCachedState();
        }
        RealSeasonsSharedData.LOGGER.info(
                "Config updated: hemisphere {}, cadence {}, interpolation {}.",
                newConfig.useSouthernHemisphere() ? "southern" : "northern",
                newConfig.cadence(),
                newConfig.interpolateTimeOfDay() ? "enabled" : "disabled"
        );
    }
}
