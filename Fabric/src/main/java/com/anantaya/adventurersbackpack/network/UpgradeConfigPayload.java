package com.anantaya.adventurersbackpack.network;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpgradeConfigPayload(
        int upgradeSlotIndex,
        int actionId
) implements CustomPacketPayload {

    public static final Type<UpgradeConfigPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    AdventurersBackpack.MOD_ID,
                    "upgrade_config"
            ));

    public static final StreamCodec<FriendlyByteBuf, UpgradeConfigPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeInt(payload.upgradeSlotIndex);
                        buf.writeInt(payload.actionId);
                    },
                    buf -> new UpgradeConfigPayload(
                            buf.readInt(),
                            buf.readInt()
                    )
            );

    public UpgradeConfigPayload(
            int upgradeSlotIndex,
            BackpackUpgradeConfigAction action
    ) {
        this(upgradeSlotIndex, action.id());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}