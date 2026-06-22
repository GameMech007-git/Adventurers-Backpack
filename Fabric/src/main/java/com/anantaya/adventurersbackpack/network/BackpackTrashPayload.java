package com.anantaya.adventurersbackpack.network;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BackpackTrashPayload() implements CustomPacketPayload {

    public static final Type<BackpackTrashPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    AdventurersBackpack.MOD_ID,
                    "creative_trash_click"
            ));

    public static final StreamCodec<FriendlyByteBuf, BackpackTrashPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                    },
                    buf -> new BackpackTrashPayload()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}