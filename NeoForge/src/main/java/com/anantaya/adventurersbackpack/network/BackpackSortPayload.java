package com.anantaya.adventurersbackpack.network;

import com.anantaya.adventurersbackpack.AdventurersBackpack;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BackpackSortPayload() implements CustomPacketPayload {

    public static final Type<BackpackSortPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    AdventurersBackpack.MOD_ID,
                    "sort_normal_storage"
            ));

    public static final StreamCodec<FriendlyByteBuf, BackpackSortPayload> CODEC =
            StreamCodec.unit(new BackpackSortPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}