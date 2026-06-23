package com.anantaya.adventurersbackpack.upgrade;

import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.registry.ModItems;
import com.anantaya.adventurersbackpack.upgrade.config.AutoPickupConfig;
import com.anantaya.adventurersbackpack.upgrade.config.FoodPouchConfig;
import com.anantaya.adventurersbackpack.upgrade.config.RecallRuneConfig;
import com.anantaya.adventurersbackpack.upgrade.config.RestockConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Set;

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
                serverPlayer.sendSystemMessage(Component.literal("Home anchor bound."));
                return true;
            }

            case SET_RECALL_WAYPOINT_1 -> {
                RecallRuneConfig.bindAnchor(
                        upgradeStack,
                        RecallRuneConfig.WAYPOINT_1,
                        serverPlayer
                );
                serverPlayer.sendSystemMessage(Component.literal("Waypoint I bound."));
                return true;
            }

            case SET_RECALL_WAYPOINT_2 -> {
                RecallRuneConfig.bindAnchor(
                        upgradeStack,
                        RecallRuneConfig.WAYPOINT_2,
                        serverPlayer
                );
                serverPlayer.sendSystemMessage(Component.literal("Waypoint II bound."));
                return true;
            }

            case TELEPORT_RECALL_HOME -> {
                teleportToAnchor(
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
                teleportToAnchor(
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
                teleportToAnchor(
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

    private static void teleportToAnchor(
            ServerPlayer player,
            Container backpackInventory,
            Container extraStorageInventory,
            BackpackTier tier,
            ItemStack upgradeStack,
            String anchorKey
    ) {
        RecallRuneConfig.Anchor anchor =
                RecallRuneConfig.getAnchor(upgradeStack, anchorKey);

        if (anchor == null) {
            player.sendSystemMessage(Component.literal("No anchor bound."));
            return;
        }

        String currentDimension =
                player.level().dimension().identifier().toString();

        if (!currentDimension.equals(anchor.dimension())) {
            player.sendSystemMessage(Component.literal("Recall Rune only works in the same dimension."));
            return;
        }

        if (isAlreadyAtAnchor(player, anchor)) {
            player.sendSystemMessage(Component.literal("You are already at this anchor."));
            return;
        }

        if (!isSafeRecallTarget(player, anchor)) {
            player.sendSystemMessage(Component.literal("Recall location is unsafe."));
            return;
        }

        if (!consumeRecallShard(
                player,
                backpackInventory,
                extraStorageInventory,
                tier
        )) {
            player.sendSystemMessage(Component.literal("A Recall Shard is required."));
            return;
        }

        player.teleportTo(
                player.level(),
                anchor.x(),
                anchor.y(),
                anchor.z(),
                Set.of(),
                anchor.yaw(),
                anchor.pitch(),
                true
        );

        player.resetFallDistance();
    }

    private static boolean isAlreadyAtAnchor(
            ServerPlayer player,
            RecallRuneConfig.Anchor anchor
    ) {
        int playerChunkX = ((int) Math.floor(player.getX())) >> 4;
        int playerChunkZ = ((int) Math.floor(player.getZ())) >> 4;

        int anchorChunkX = ((int) Math.floor(anchor.x())) >> 4;
        int anchorChunkZ = ((int) Math.floor(anchor.z())) >> 4;

        return playerChunkX == anchorChunkX
                && playerChunkZ == anchorChunkZ;
    }

    private static boolean isSafeRecallTarget(
            ServerPlayer player,
            RecallRuneConfig.Anchor anchor
    ) {
        AABB targetBox = player.getBoundingBox().move(
                anchor.x() - player.getX(),
                anchor.y() - player.getY(),
                anchor.z() - player.getZ()
        );

        if (!player.level().noCollision(player, targetBox)) {
            return false;
        }

        if (player.level().containsAnyLiquid(targetBox)) {
            return false;
        }

        BlockPos belowPos = BlockPos.containing(
                anchor.x(),
                anchor.y() - 0.1D,
                anchor.z()
        );

        return !player.level().getBlockState(belowPos).isAir();
    }

    private static boolean consumeRecallShard(
            ServerPlayer player,
            Container backpackInventory,
            Container extraStorageInventory,
            BackpackTier tier
    ) {
        if (consumeRecallShardFromPlayer(player)) {
            return true;
        }

        if (consumeRecallShardFromBackpackProtectedAndNormalStorage(
                backpackInventory,
                tier
        )) {
            return true;
        }

        return consumeRecallShardFromExtraStorage(extraStorageInventory);
    }

    private static boolean consumeRecallShardFromPlayer(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.is(ModItems.RECALL_SHARD)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }

        return false;
    }

    private static boolean consumeRecallShardFromBackpackProtectedAndNormalStorage(
            Container backpackInventory,
            BackpackTier tier
    ) {
        for (int i = 0; i < tier.upgradeStart(); i++) {
            ItemStack stack = backpackInventory.getItem(i);

            if (stack.is(ModItems.RECALL_SHARD)) {
                stack.shrink(1);
                backpackInventory.setChanged();
                return true;
            }
        }

        return false;
    }

    private static boolean consumeRecallShardFromExtraStorage(
            Container extraStorageInventory
    ) {
        if (extraStorageInventory == null) {
            return false;
        }

        for (int i = 0; i < extraStorageInventory.getContainerSize(); i++) {
            ItemStack stack = extraStorageInventory.getItem(i);

            if (stack.is(ModItems.RECALL_SHARD)) {
                stack.shrink(1);
                extraStorageInventory.setChanged();
                return true;
            }
        }

        return false;
    }
}