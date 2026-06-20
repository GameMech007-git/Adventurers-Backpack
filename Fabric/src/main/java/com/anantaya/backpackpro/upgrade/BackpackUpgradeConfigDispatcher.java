package com.anantaya.backpackpro.upgrade;

import com.anantaya.backpackpro.backpack.BackpackTier;

import com.anantaya.backpackpro.upgrade.config.AutoPickupConfig;
import com.anantaya.backpackpro.upgrade.config.FoodPouchConfig;
import com.anantaya.backpackpro.upgrade.config.RestockConfig;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class BackpackUpgradeConfigDispatcher {

    private BackpackUpgradeConfigDispatcher() {
    }

    public static void apply(
            Container inventory,
            BackpackTier tier,
            int upgradeSlotIndex,
            BackpackUpgradeConfigAction action
    ) {
        if (upgradeSlotIndex < tier.upgradeStart()
                || upgradeSlotIndex >= tier.totalSlots) {
            return;
        }

        ItemStack upgradeStack = inventory.getItem(upgradeSlotIndex);

        if (!(upgradeStack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return;
        }

        boolean changed = switch (upgradeItem.getType()) {
            case AUTO_PICKUP -> applyAutoPickup(upgradeStack, action);
            case FOOD_POUCH -> applyFoodPouch(upgradeStack, action);
            case RESTOCK -> applyRestock(upgradeStack, action);
            case LANTERN_HOOK, FLUID_STORAGE, EXTRA_STORAGE, CRAFTING -> false;
        };

        if (!changed) {
            return;
        }

        inventory.setItem(upgradeSlotIndex, upgradeStack);
        inventory.setChanged();
    }

    private static boolean applyAutoPickup(
            ItemStack upgradeStack,
            BackpackUpgradeConfigAction action
    ) {
        switch (action) {
            case CYCLE_AUTO_PICKUP_MODE -> {
                AutoPickupConfig.cycleMode(upgradeStack);
                return true;
            }

            case TOGGLE_IGNORE_PLAYER_DROPS -> {
                AutoPickupConfig.toggleIgnorePlayerDrops(upgradeStack);
                return true;
            }

            default -> {
                return false;
            }
        }
    }

    private static boolean applyFoodPouch(
            ItemStack upgradeStack,
            BackpackUpgradeConfigAction action
    ) {
        switch (action) {
            case CYCLE_FOOD_POUCH_MODE -> {
                FoodPouchConfig.cycleMode(upgradeStack);
                return true;
            }

            case TOGGLE_FOOD_POUCH_EMPTY_WARNING -> {
                FoodPouchConfig.toggleEmptyWarning(upgradeStack);
                return true;
            }

            default -> {
                return false;
            }
        }
    }

    private static boolean applyRestock(
            ItemStack upgradeStack,
            BackpackUpgradeConfigAction action
    ) {
        switch (action) {

            case TOGGLE_RESTOCK_TOOLS -> {
                RestockConfig.toggleTools(upgradeStack);
                return true;
            }

            case TOGGLE_RESTOCK_BUCKETS -> {
                RestockConfig.toggleBuckets(upgradeStack);
                return true;
            }

            default -> {
                return false;
            }
        }
    }
}