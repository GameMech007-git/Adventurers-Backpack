package com.anantaya.adventurersbackpack.network;

import com.anantaya.adventurersbackpack.AdventurersBackpack;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FluidStoragePayload() implements CustomPacketPayload {

    public static final Type<FluidStoragePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    AdventurersBackpack.MOD_ID,
                    "fluid_storage_click"
            ));

    public static final StreamCodec<FriendlyByteBuf, FluidStoragePayload> CODEC =
            StreamCodec.unit(new FluidStoragePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}