package com.codex.realseasons.server;

import com.codex.realseasons.config.RealSeasonsCadence;
import com.codex.realseasons.config.RealSeasonsCommonConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import sereneseasons.init.ModConfig;
import sereneseasons.season.SeasonTime;

import com.codex.realseasons.network.RealSeasonsPackets;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Adjusts Serene Seasons' displayed sub-season length to align UI with our cadence,
 * and broadcasts both display days and season state to clients.
 */
public final class RealSeasonsDisplayLinker {
    private int lastAppliedDays = -1;

    public void maybeApply(MinecraftServer server, RealSeasonsCommonConfig config, SeasonTime seasonState) {
        int desired = desiredDays(config);
        if (desired <= 0) return;

        if (ModConfig.seasons.subSeasonDuration != desired) {
            ModConfig.seasons.subSeasonDuration = desired;
        }

        if (lastAppliedDays != desired) {
            broadcastToClients(server, desired, seasonState);
            lastAppliedDays = desired;
        }
    }

    public void broadcastSeasonState(MinecraftServer server, SeasonTime seasonState) {
        var statePayload = new RealSeasonsPackets.SeasonStatePayload(seasonState.getSeasonCycleTicks());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, statePayload);
        }
    }

    private int desiredDays(RealSeasonsCommonConfig config) {
        return switch (config.cadence()) {
            case MONTH -> LocalDate.now(config.zoneId()).lengthOfMonth();
            case WEEK -> 7;
            case DAY -> 1;
        };
    }

    private void broadcastToClients(MinecraftServer server, int days, SeasonTime seasonState) {
        var daysPayload = new RealSeasonsPackets.DisplayDaysPayload(days);
        var statePayload = new RealSeasonsPackets.SeasonStatePayload(seasonState.getSeasonCycleTicks());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, daysPayload);
            ServerPlayNetworking.send(player, statePayload);
        }
    }
}
