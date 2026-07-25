package com.anantaya.adventurersbackpack.upgrade.autopickup;

import com.anantaya.adventurersbackpack.backpack.BackpackInventory;
import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackScreenHandler;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.storage.BackpackStorageHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeFinder;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.config.AutoPickupConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class BackpackAutoPickupHelper {

    private static final double PICKUP_RANGE = 2.25D;

    private BackpackAutoPickupHelper() {
    }

    public static void tick(ServerPlayer player) {
        if (player == null || player.isSpectator()) {
            return;
        }

        if (player.containerMenu instanceof BackpackScreenHandler) {
            return;
        }

        RegistryAccess registryAccess = player.level().registryAccess();

        ItemStack backpackStack = BackpackUpgradeFinder.findBackpackWithUpgrade(
                player,
                BackpackUpgradeItem.Type.AUTO_PICKUP,
                registryAccess
        );

        if (backpackStack.isEmpty()) {
            return;
        }

        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        BackpackTier tier = backpackItem.getTier();

        BackpackInventory backpackInventory = new BackpackInventory(
                backpackStack,
                tier.totalSlots,
                registryAccess
        );

        ItemStack autoPickupUpgrade = BackpackUpgradeFinder.findUpgradeStack(
                backpackInventory,
                tier,
                BackpackUpgradeItem.Type.AUTO_PICKUP
        );

        if (autoPickupUpgrade.isEmpty()) {
            return;
        }

        AutoPickupMode mode = AutoPickupConfig.getMode(autoPickupUpgrade);

        if (mode == AutoPickupMode.OFF) {
            return;
        }

        boolean ignorePlayerDrops =
                AutoPickupConfig.shouldIgnorePlayerDrops(autoPickupUpgrade);

        AABB pickupBox = player.getBoundingBox().inflate(PICKUP_RANGE);

        List<ItemEntity> nearbyItems = player.level().getEntitiesOfClass(
                ItemEntity.class,
                pickupBox,
                itemEntity -> itemEntity.isAlive()
                        && !itemEntity.getItem().isEmpty()
        );

        if (nearbyItems.isEmpty()) {
            return;
        }

        boolean changed = false;

        for (ItemEntity itemEntity : nearbyItems) {
            ItemStack worldStack = itemEntity.getItem();

            if (!canAutoPickup(worldStack)) {
                continue;
            }

            boolean droppedByThisPlayer = wasDroppedByThisPlayer(itemEntity, player);

            if (ignorePlayerDrops && droppedByThisPlayer) {
                continue;
            }

            if (mode == AutoPickupMode.MATCHING_ONLY
                    && !BackpackStorageHelper.hasMatchingItemInNormalStorage(
                    backpackInventory,
                    tier,
                    worldStack
            )) {
                continue;
            }

            ItemStack before = worldStack.copy();

            BackpackStorageHelper.InsertResult result = BackpackStorageHelper.insertIntoNormalStorage(
                    backpackInventory,
                    tier,
                    before
            );

            if (result.insertedCount() <= 0) {
                continue;
            }

            changed = true;

            if (result.remaining().isEmpty()) {
                System.out.println(
                        "[Backpack AutoPickup] Picked full stack: "
                                + before
                                + " inserted="
                                + result.insertedCount()
                );

                itemEntity.discard();
            } else {
                System.out.println(
                        "[Backpack AutoPickup] Picked partial stack: "
                                + before
                                + " inserted="
                                + result.insertedCount()
                                + " remaining="
                                + result.remaining()
                );

                itemEntity.setItem(result.remaining());
            }
        }

        if (changed) {
            backpackInventory.saveToData();
        }
    }

    private static boolean canAutoPickup(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof BackpackItem) {
            return false;
        }

        if (stack.getItem() instanceof BackpackUpgradeItem) {
            return false;
        }

        return true;
    }

    private static boolean wasDroppedByThisPlayer(
            ItemEntity itemEntity,
            ServerPlayer player
    ) {
        Entity owner = itemEntity.getOwner();

        return owner != null && owner.getUUID().equals(player.getUUID());
    }
}