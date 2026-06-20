package com.anantaya.backpackpro.upgrade;

import com.anantaya.backpackpro.backpack.BackpackItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BackpackPlayerUpgradeHelper {

    private BackpackPlayerUpgradeHelper() {
    }

    public static boolean hasLanternHookBackpack(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
                continue;
            }

            if (BackpackUpgradeHelper.hasUpgrade(
                    stack,
                    BackpackUpgradeItem.Type.LANTERN_HOOK,
                    backpackItem.getTier(),
                    player.level().registryAccess()
            )) {
                return true;
            }
        }

        return false;
    }
}