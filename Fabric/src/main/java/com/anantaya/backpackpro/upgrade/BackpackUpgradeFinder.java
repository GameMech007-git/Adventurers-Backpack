package com.anantaya.backpackpro.upgrade;

import com.anantaya.backpackpro.backpack.BackpackInventory;
import com.anantaya.backpackpro.backpack.BackpackItem;
import com.anantaya.backpackpro.backpack.BackpackTier;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class BackpackUpgradeFinder {

    private BackpackUpgradeFinder() {
    }

    public static ItemStack findBackpackWithUpgrade(
            ServerPlayer player,
            BackpackUpgradeItem.Type upgradeType,
            RegistryAccess registryAccess
    ) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
                continue;
            }

            boolean hasUpgrade = BackpackUpgradeHelper.hasUpgrade(
                    stack,
                    upgradeType,
                    backpackItem.getTier(),
                    registryAccess
            );

            if (hasUpgrade) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack findUpgradeStack(
            BackpackInventory backpackInventory,
            BackpackTier tier,
            BackpackUpgradeItem.Type upgradeType
    ) {
        for (int i = 0; i < tier.upgradeSlots; i++) {
            int slotIndex = tier.upgradeStart() + i;
            ItemStack stack = backpackInventory.getItem(slotIndex);

            if (stack.getItem() instanceof BackpackUpgradeItem upgradeItem
                    && upgradeItem.getType() == upgradeType) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}