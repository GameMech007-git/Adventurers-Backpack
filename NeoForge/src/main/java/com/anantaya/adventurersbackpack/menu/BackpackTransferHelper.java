package com.anantaya.adventurersbackpack.menu;

import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;

import net.minecraft.world.item.ItemStack;

public final class BackpackTransferHelper {

    private BackpackTransferHelper() {
    }

    public static TransferTarget getTarget(
            BackpackTier tier,
            int slotIndex,
            int totalMenuSlots,
            ItemStack stack,
            int extraStorageStart,
            int extraStorageEnd
    ) {
        int backpackMainEnd = tier.totalSlots;
        int playerStart = extraStorageEnd;
        int playerEnd = totalMenuSlots;

        boolean hasExtraStorage =
                extraStorageEnd > extraStorageStart;

        boolean isMainBackpackSlot =
                slotIndex >= 0 && slotIndex < backpackMainEnd;

        boolean isExtraStorageSlot =
                hasExtraStorage
                        && slotIndex >= extraStorageStart
                        && slotIndex < extraStorageEnd;

        if (isMainBackpackSlot || isExtraStorageSlot) {
            return new TransferTarget(playerStart, playerEnd, true);
        }

        if (stack.getItem() instanceof BackpackUpgradeItem) {
            return new TransferTarget(
                    tier.upgradeStart(),
                    tier.totalSlots,
                    false
            );
        }

        return new TransferTarget(
                tier.normalStart(),
                tier.upgradeStart(),
                false
        );
    }

    public record TransferTarget(
            int start,
            int end,
            boolean reverse
    ) {
    }
}