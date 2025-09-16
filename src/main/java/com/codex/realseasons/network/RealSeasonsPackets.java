package com.codex.realseasons.network;

import com.codex.realseasons.RealSeasonsSharedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class RealSeasonsPackets {
    public static final ResourceLocation ID_DISPLAY_DAYS = ResourceLocation.fromNamespaceAndPath(RealSeasonsSharedData.MOD_ID, "display_days");

    public record DisplayDaysPayload(int days) implements CustomPacketPayload {
        public static final Type<DisplayDaysPayload> TYPE = new Type<>(ID_DISPLAY_DAYS);
        public static final StreamCodec<FriendlyByteBuf, DisplayDaysPayload> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public DisplayDaysPayload decode(FriendlyByteBuf buf) {
                return new DisplayDaysPayload(buf.readVarInt());
            }

            @Override
            public void encode(FriendlyByteBuf buf, DisplayDaysPayload value) {
                buf.writeVarInt(value.days);
            }
        };

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private RealSeasonsPackets() {}
}
