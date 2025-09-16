package com.codex.realseasons.server;

import com.codex.realseasons.config.RealSeasonsCadence;
import com.codex.realseasons.config.RealSeasonsCommonConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import sereneseasons.init.ModConfig;

import com.codex.realseasons.network.RealSeasonsPackets;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Adjusts Serene Seasons' displayed sub-season length to align UI with our cadence,
 * and broadcasts the value to clients so their tooltips match.
 */
public final class RealSeasonsDisplayLinker {
    private int lastAppliedDays = -1;

    public void maybeApply(MinecraftServer server, RealSeasonsCommonConfig config) {
        int desired = desiredDays(config);
        if (desired <= 0) return;

        if (ModConfig.seasons.subSeasonDuration != desired) {
            ModConfig.seasons.subSeasonDuration = desired;
        }

        if (lastAppliedDays != desired) {
            broadcastDisplayDays(server, desired);
            lastAppliedDays = desired;
        }
    }

    private int desiredDays(RealSeasonsCommonConfig config) {
        return switch (config.cadence()) {
            case MONTH -> LocalDate.now(config.zoneId()).lengthOfMonth();
            case WEEK -> 7;
            case DAY -> 1;
        };
    }

    private void broadcastDisplayDays(MinecraftServer server, int days) {
        var payload = new RealSeasonsPackets.DisplayDaysPayload(days);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
