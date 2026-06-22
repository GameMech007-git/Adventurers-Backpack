package com.anantaya.adventurersbackpack.storage;

import com.anantaya.adventurersbackpack.backpack.BackpackInventory;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;

import net.minecraft.world.item.ItemStack;

public final class BackpackStorageHelper {

    private BackpackStorageHelper() {
    }

    public static boolean hasMatchingItemInNormalStorage(
            BackpackInventory backpackInventory,
            BackpackTier tier,
            ItemStack incomingStack
    ) {
        for (int i = 0; i < tier.normalSlots; i++) {
            int slotIndex = tier.normalStart() + i;
            ItemStack existing = backpackInventory.getItem(slotIndex);

            if (existing.isEmpty()) {
                continue;
            }

            if (ItemStack.isSameItemSameComponents(existing, incomingStack)) {
                return true;
            }
        }

        return false;
    }

    public static InsertResult insertIntoNormalStorage(
            BackpackInventory backpackInventory,
            BackpackTier tier,
            ItemStack incomingStack
    ) {
        ItemStack remaining = incomingStack.copy();
        int originalCount = remaining.getCount();

        mergeIntoExistingStacks(backpackInventory, tier, remaining);
        insertIntoEmptySlots(backpackInventory, tier, remaining);

        int insertedCount = originalCount - remaining.getCount();

        return new InsertResult(remaining, insertedCount);
    }

    private static void mergeIntoExistingStacks(
            BackpackInventory backpackInventory,
            BackpackTier tier,
            ItemStack remaining
    ) {
        for (int i = 0; i < tier.normalSlots; i++) {
            if (remaining.isEmpty()) {
                return;
            }

            int slotIndex = tier.normalStart() + i;
            ItemStack existing = backpackInventory.getItem(slotIndex);

            if (existing.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameComponents(existing, remaining)) {
                continue;
            }

            int space = existing.getMaxStackSize() - existing.getCount();

            if (space <= 0) {
                continue;
            }

            int moveAmount = Math.min(space, remaining.getCount());

            ItemStack updated = existing.copy();
            updated.grow(moveAmount);

            remaining.shrink(moveAmount);

            backpackInventory.setItem(slotIndex, updated);
        }
    }

    private static void insertIntoEmptySlots(
            BackpackInventory backpackInventory,
            BackpackTier tier,
            ItemStack remaining
    ) {
        for (int i = 0; i < tier.normalSlots; i++) {
            if (remaining.isEmpty()) {
                return;
            }

            int slotIndex = tier.normalStart() + i;
            ItemStack existing = backpackInventory.getItem(slotIndex);

            if (!existing.isEmpty()) {
                continue;
            }

            int moveAmount = Math.min(
                    remaining.getMaxStackSize(),
                    remaining.getCount()
            );

            ItemStack inserted = remaining.copy();
            inserted.setCount(moveAmount);

            remaining.shrink(moveAmount);

            backpackInventory.setItem(slotIndex, inserted);
        }
    }

    public record InsertResult(
            ItemStack remaining,
            int insertedCount
    ) {
    }
}