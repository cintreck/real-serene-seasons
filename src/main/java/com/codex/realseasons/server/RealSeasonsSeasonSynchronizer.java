package com.codex.realseasons.server;

import com.codex.realseasons.RealSeasonsSharedData;
import com.codex.realseasons.calendar.RealSeasonsCalendarService;
import com.codex.realseasons.calendar.RealSeasonsCalendarService.RealSeasonsCalendarSnapshot;
import com.codex.realseasons.config.RealSeasonsCommonConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import sereneseasons.api.SSGameRules;
import sereneseasons.init.ModConfig;
import sereneseasons.season.SeasonHandler;
import sereneseasons.season.SeasonSavedData;
import sereneseasons.season.SeasonTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Keeps Serene Seasons' world data aligned with the real-world calendar.
 */
public final class RealSeasonsSeasonSynchronizer {
    private final Supplier<RealSeasonsCommonConfig> configSupplier;
    private final RealSeasonsCalendarService calendarService;
    private final RealSeasonsSeasonStateStore stateStore;
    private final RealSeasonsDisplayLinker displayLinker;

    private final Map<ResourceKey<Level>, Integer> lastBroadcastTicks = new HashMap<>();
    private final Set<ResourceKey<Level>> disabledCycleRule = new HashSet<>();

    public RealSeasonsSeasonSynchronizer(Supplier<RealSeasonsCommonConfig> configSupplier,
                                         RealSeasonsCalendarService calendarService,
                                         RealSeasonsSeasonStateStore stateStore) {
        this.configSupplier = Objects.requireNonNull(configSupplier, "configSupplier");
        this.calendarService = Objects.requireNonNull(calendarService, "calendarService");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
        this.displayLinker = new RealSeasonsDisplayLinker();
    }

    public void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> syncAll(server, true));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> resetCachedState());
        ServerTickEvents.END_SERVER_TICK.register(server -> syncAll(server, false));
    }

    public void resetCachedState() {
        lastBroadcastTicks.clear();
        disabledCycleRule.clear();
    }

    private void syncAll(MinecraftServer server, boolean forceBroadcast) {
        RealSeasonsCommonConfig config = configSupplier.get();
        // Ensure Serene Seasons' UI day count tracks our cadence (MONTH/WEEK/DAY)
        displayLinker.maybeApply(server, config);
        RealSeasonsCalendarSnapshot snapshot = calendarService.snapshot(config);
        SeasonTime desiredState = snapshot.seasonTime();
        stateStore.update(desiredState);

        for (ServerLevel level : server.getAllLevels()) {
            if (!ModConfig.seasons.isDimensionWhitelisted(level.dimension())) {
                continue;
            }

            disableSeasonCycle(level);
            applySeasonState(level, desiredState, forceBroadcast);
        }
    }

    private void disableSeasonCycle(ServerLevel level) {
        if (disabledCycleRule.contains(level.dimension())) {
            return;
        }

        GameRules.BooleanValue rule = level.getGameRules().getRule(SSGameRules.RULE_DOSEASONCYCLE);
        if (rule != null && rule.get()) {
            rule.set(false, level.getServer());
            RealSeasonsSharedData.LOGGER.info("Disabled doSeasonCycle game rule in {} to allow real-time season syncing.", level.dimension().location());
        }
        disabledCycleRule.add(level.dimension());
    }

    private void applySeasonState(ServerLevel level, SeasonTime desiredState, boolean forceBroadcast) {
        SeasonSavedData savedData = SeasonHandler.getSeasonSavedData(level);
        if (savedData == null) {
            return;
        }

        int targetTick = desiredState.getSeasonCycleTicks();
        boolean changed = savedData.seasonCycleTicks != targetTick;
        if (changed) {
            savedData.seasonCycleTicks = targetTick;
            savedData.setDirty();
        }

        SeasonHandler.prevServerSeasonCycleTicks.put(level.dimension(), targetTick);

        Integer previous = lastBroadcastTicks.get(level.dimension());
        if (forceBroadcast || changed || previous == null || previous != targetTick) {
            SeasonHandler.sendSeasonUpdate(level);
            lastBroadcastTicks.put(level.dimension(), targetTick);
        }
    }
}
