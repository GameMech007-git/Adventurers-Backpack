package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.menu.BackpackSortHelper;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.ExtraStorageMenuHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public final class BackpackMenuSortHandler {

    private BackpackMenuSortHandler() {
    }

    public static boolean sortNormalStorage(
            Player player,
            Container inventory,
            BackpackTier tier,
            Container extraStorageInventory,
            boolean canUseExtraStorageSlots,
            ExtraStorageMenuHandler extraStorageMenuHandler
    ) {
        if (player == null) {
            return false;
        }

        boolean changed = BackpackSortHelper.sortAllSections(
                inventory,
                tier,
                extraStorageInventory,
                canUseExtraStorageSlots,
                player.getInventory()
        );

        if (!changed) {
            return false;
        }

        if (inventory instanceof BackpackInventory backpackInventory) {
            backpackInventory.saveToData();
        } else {
            inventory.setChanged();
        }

        extraStorageMenuHandler.setChanged();
        player.getInventory().setChanged();

        return true;
    }
}