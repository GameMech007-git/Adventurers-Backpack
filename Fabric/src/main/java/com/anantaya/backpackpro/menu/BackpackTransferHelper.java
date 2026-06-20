package com.anantaya.backpackpro.menu;

import com.anantaya.backpackpro.backpack.BackpackTier;
import com.anantaya.backpackpro.upgrade.BackpackUpgradeItem;

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

        /*
         * First implementation:
         * player inventory -> main normal storage only.
         * Extra storage remains manual, so automation and quick-move
         * cannot unexpectedly fill it.
         */
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