package com.anantaya.adventurersbackpack.upgrade;

import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.config.AutoPickupConfig;
import com.anantaya.adventurersbackpack.upgrade.config.FoodPouchConfig;
import com.anantaya.adventurersbackpack.upgrade.config.RecallRuneConfig;
import com.anantaya.adventurersbackpack.upgrade.config.RestockConfig;
import com.anantaya.adventurersbackpack.upgrade.recallrune.RecallRunePendingManager;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BackpackUpgradeConfigDispatcher {

    private BackpackUpgradeConfigDispatcher() {
    }

    public static void apply(
            Player player,
            Container inventory,
            Container extraStorageInventory,
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
            case RECALL_RUNE -> applyRecallRune(
                    player,
                    inventory,
                    extraStorageInventory,
                    tier,
                    upgradeStack,
                    action
            );
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

    private static boolean applyRecallRune(
            Player player,
            Container backpackInventory,
            Container extraStorageInventory,
            BackpackTier tier,
            ItemStack upgradeStack,
            BackpackUpgradeConfigAction action
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        switch (action) {
            case SET_RECALL_HOME -> {
                RecallRuneConfig.bindAnchor(
                        upgradeStack,
                        RecallRuneConfig.HOME,
                        serverPlayer
                );

                serverPlayer.sendSystemMessage(
                        Component.literal("Home anchor bound.")
                );

                return true;
            }

            case SET_RECALL_WAYPOINT_1 -> {
                RecallRuneConfig.bindAnchor(
                        upgradeStack,
                        RecallRuneConfig.WAYPOINT_1,
                        serverPlayer
                );

                serverPlayer.sendSystemMessage(
                        Component.literal("Waypoint I bound.")
                );

                return true;
            }

            case SET_RECALL_WAYPOINT_2 -> {
                RecallRuneConfig.bindAnchor(
                        upgradeStack,
                        RecallRuneConfig.WAYPOINT_2,
                        serverPlayer
                );

                serverPlayer.sendSystemMessage(
                        Component.literal("Waypoint II bound.")
                );

                return true;
            }

            case TELEPORT_RECALL_HOME -> {
                RecallRunePendingManager.startRecall(
                        serverPlayer,
                        backpackInventory,
                        extraStorageInventory,
                        tier,
                        upgradeStack,
                        RecallRuneConfig.HOME
                );

                return false;
            }

            case TELEPORT_RECALL_WAYPOINT_1 -> {
                RecallRunePendingManager.startRecall(
                        serverPlayer,
                        backpackInventory,
                        extraStorageInventory,
                        tier,
                        upgradeStack,
                        RecallRuneConfig.WAYPOINT_1
                );

                return false;
            }

            case TELEPORT_RECALL_WAYPOINT_2 -> {
                RecallRunePendingManager.startRecall(
                        serverPlayer,
                        backpackInventory,
                        extraStorageInventory,
                        tier,
                        upgradeStack,
                        RecallRuneConfig.WAYPOINT_2
                );

                return false;
            }

            default -> {
                return false;
            }
        }
    }
}