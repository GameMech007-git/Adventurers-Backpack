package com.anantaya.adventurersbackpack.menu;

import com.anantaya.adventurersbackpack.backpack.BackpackInventory;
import com.anantaya.adventurersbackpack.backpack.BackpackMenuLayout;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.ExtraStorageInventory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class BackpackUnloadHelper {

    private BackpackUnloadHelper() {
    }

    public static void unloadBackpackToContainer(
            ItemStack backpackStack,
            Container target,
            BackpackTier tier,
            RegistryAccess registryAccess
    ) {
        if (backpackStack.isEmpty()) {
            return;
        }

        boolean movedAnything = false;

        boolean hasExtraStorage = BackpackUpgradeHelper.hasUpgrade(
                backpackStack,
                BackpackUpgradeItem.Type.EXTRA_STORAGE,
                tier,
                registryAccess
        );

        if (hasExtraStorage) {
            ExtraStorageInventory extraStorageInventory = new ExtraStorageInventory(
                    backpackStack,
                    BackpackMenuLayout.extraStorageSlotsForTier(tier),
                    registryAccess
            );

            if (unloadRange(
                    extraStorageInventory,
                    0,
                    extraStorageInventory.getContainerSize(),
                    target
            )) {
                movedAnything = true;
                extraStorageInventory.saveToData();
            }
        }

        BackpackInventory backpackInventory = new BackpackInventory(
                backpackStack,
                tier.totalSlots,
                registryAccess
        );

        if (unloadRange(
                backpackInventory,
                0,
                tier.protectedSlots,
                target
        )) {
            movedAnything = true;
        }

        if (unloadRange(
                backpackInventory,
                tier.normalStart(),
                tier.upgradeStart(),
                target
        )) {
            movedAnything = true;
        }

        if (movedAnything) {
            backpackInventory.saveToData();
            target.setChanged();
        }

    }

    private static boolean unloadRange(
            Container source,
            int startSlotInclusive,
            int endSlotExclusive,
            Container target
    ) {
        boolean movedAnything = false;

        for (int slot = startSlotInclusive; slot < endSlotExclusive; slot++) {
            ItemStack sourceStack = source.getItem(slot);

            if (sourceStack.isEmpty()) {
                continue;
            }

            ItemStack remainder = insertIntoTarget(sourceStack, target);

            if (remainder.getCount() != sourceStack.getCount()) {
                movedAnything = true;
                source.setItem(slot, remainder);
            }

            if (remainder.isEmpty()) {
                continue;
            }
        }

        if (movedAnything) {
            source.setChanged();
        }

        return movedAnything;
    }

    private static ItemStack insertIntoTarget(
            ItemStack stackToInsert,
            Container target
    ) {
        if (stackToInsert.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack remaining = stackToInsert.copy();

        mergeIntoExistingStacks(remaining, target);

        if (remaining.isEmpty()) {
            return ItemStack.EMPTY;
        }

        fillEmptySlots(remaining, target);

        return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
    }

    private static void mergeIntoExistingStacks(
            ItemStack remaining,
            Container target
    ) {
        for (int targetSlot = 0; targetSlot < target.getContainerSize(); targetSlot++) {
            if (remaining.isEmpty()) {
                return;
            }

            ItemStack targetStack = target.getItem(targetSlot);

            if (targetStack.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameComponents(targetStack, remaining)) {
                continue;
            }

            if (!target.canPlaceItem(targetSlot, remaining)) {
                continue;
            }

            int maxStackSize = target.getMaxStackSize(targetStack);
            int space = maxStackSize - targetStack.getCount();

            if (space <= 0) {
                continue;
            }

            int moveCount = Math.min(space, remaining.getCount());

            targetStack.setCount(targetStack.getCount() + moveCount);
            remaining.setCount(remaining.getCount() - moveCount);

            target.setItem(targetSlot, targetStack);
        }
    }

    private static void fillEmptySlots(
            ItemStack remaining,
            Container target
    ) {
        for (int targetSlot = 0; targetSlot < target.getContainerSize(); targetSlot++) {
            if (remaining.isEmpty()) {
                return;
            }

            ItemStack targetStack = target.getItem(targetSlot);

            if (!targetStack.isEmpty()) {
                continue;
            }

            if (!target.canPlaceItem(targetSlot, remaining)) {
                continue;
            }

            int maxStackSize = target.getMaxStackSize(remaining);
            int moveCount = Math.min(maxStackSize, remaining.getCount());

            if (moveCount <= 0) {
                continue;
            }

            ItemStack inserted = remaining.copy();
            inserted.setCount(moveCount);

            target.setItem(targetSlot, inserted);
            remaining.setCount(remaining.getCount() - moveCount);
        }
    }
}