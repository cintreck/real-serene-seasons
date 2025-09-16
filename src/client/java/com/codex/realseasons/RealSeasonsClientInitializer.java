package com.codex.realseasons;

import com.codex.realseasons.network.RealSeasonsPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import sereneseasons.init.ModConfig;

/**
 * Receives display day length updates so client UI reflects server cadence.
 */
public final class RealSeasonsClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(RealSeasonsPackets.DisplayDaysPayload.TYPE, RealSeasonsPackets.DisplayDaysPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(RealSeasonsPackets.DisplayDaysPayload.TYPE, (payload, context) -> {
            int days = payload.days();
            context.client().execute(() -> ModConfig.seasons.subSeasonDuration = days);
        });
    }
}
