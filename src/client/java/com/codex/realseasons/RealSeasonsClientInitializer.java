package com.codex.realseasons;

import com.codex.realseasons.network.RealSeasonsPackets;
import com.codex.realseasons.server.RealSeasonsSeasonStateStore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import sereneseasons.init.ModConfig;
import sereneseasons.season.SeasonTime;

/**
 * Receives display day length and season state updates so client matches server.
 */
public final class RealSeasonsClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(RealSeasonsPackets.DisplayDaysPayload.TYPE, RealSeasonsPackets.DisplayDaysPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(RealSeasonsPackets.SeasonStatePayload.TYPE, RealSeasonsPackets.SeasonStatePayload.STREAM_CODEC);

        ClientPlayNetworking.registerGlobalReceiver(RealSeasonsPackets.DisplayDaysPayload.TYPE, (payload, context) -> {
            int days = payload.days();
            context.client().execute(() -> ModConfig.seasons.subSeasonDuration = days);
        });

        ClientPlayNetworking.registerGlobalReceiver(RealSeasonsPackets.SeasonStatePayload.TYPE, (payload, context) -> {
            int ticks = payload.seasonCycleTicks();
            context.client().execute(() -> {
                RealSeasonsSeasonStateStore store = RealSeasonsModInitializer.stateStore();
                if (store != null) {
                    store.update(new SeasonTime(ticks));
                }
            });
        });
    }
}
