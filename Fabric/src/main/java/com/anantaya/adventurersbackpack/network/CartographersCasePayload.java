package com.anantaya.adventurersbackpack.network;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CartographersCasePayload(
        int upgradeSlotIndex,
        int navigationSlot
) implements CustomPacketPayload {

    public static final Type<CartographersCasePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    AdventurersBackpack.MOD_ID,
                    "cartographers_case"
            ));

    public static final StreamCodec<FriendlyByteBuf, CartographersCasePayload> CODEC =
            StreamCodec.of(
                    (FriendlyByteBuf buf, CartographersCasePayload payload) -> {
                        buf.writeInt(payload.upgradeSlotIndex());
                        buf.writeInt(payload.navigationSlot());
                    },
                    (FriendlyByteBuf buf) -> new CartographersCasePayload(
                            buf.readInt(),
                            buf.readInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}