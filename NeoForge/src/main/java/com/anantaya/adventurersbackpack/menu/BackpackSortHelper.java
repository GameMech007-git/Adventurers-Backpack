package com.anantaya.adventurersbackpack.menu;

import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BackpackSortHelper {

    private BackpackSortHelper() {
    }

    public static boolean sortContainerRange(
            Container inventory,
            int start,
            int end,
            Comparator<ItemStack> comparator
    ) {
        List<ItemStack> collected = collectStacks(
                inventory,
                start,
                end
        );

        if (collected.isEmpty()) {
            return false;
        }

        List<ItemStack> merged = mergeCompatibleStacks(collected);
        merged.sort(comparator);

        return writeSortedStacks(
                inventory,
                start,
                end,
                merged
        );
    }

    private static List<ItemStack> collectStacks(
            Container inventory,
            int start,
            int end
    ){
        List<ItemStack> stacks = new ArrayList<>();

        for (int slot = start; slot < end; slot++) {
            ItemStack stack = inventory.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            stacks.add(stack.copy());
        }

        return stacks;
    }

    private static List<ItemStack> mergeCompatibleStacks(
            List<ItemStack> input
    ) {
        List<ItemStack> merged = new ArrayList<>();

        for (ItemStack source : input) {
            ItemStack remaining = source.copy();

            mergeIntoExistingStacks(merged, remaining);

            while (!remaining.isEmpty()) {
                int moveAmount = Math.min(
                        remaining.getMaxStackSize(),
                        remaining.getCount()
                );

                ItemStack split = remaining.copy();
                split.setCount(moveAmount);

                remaining.shrink(moveAmount);
                merged.add(split);
            }
        }

        return merged;
    }

    private static void mergeIntoExistingStacks(
            List<ItemStack> merged,
            ItemStack remaining
    ) {
        for (ItemStack existing : merged) {
            if (remaining.isEmpty()) {
                return;
            }

            if (!ItemStack.isSameItemSameComponents(existing, remaining)) {
                continue;
            }

            int space = existing.getMaxStackSize() - existing.getCount();

            if (space <= 0) {
                continue;
            }

            int moveAmount = Math.min(space, remaining.getCount());

            existing.grow(moveAmount);
            remaining.shrink(moveAmount);
        }
    }

    private static boolean writeSortedStacks(
            Container inventory,
            int start,
            int end,
            List<ItemStack> sortedStacks
    ) {
        boolean changed = false;
        int outputIndex = 0;

        for (int slot = start; slot < end; slot++) {
            ItemStack newStack = outputIndex < sortedStacks.size()
                    ? sortedStacks.get(outputIndex).copy()
                    : ItemStack.EMPTY;

            ItemStack oldStack = inventory.getItem(slot);

            if (!ItemStack.matches(oldStack, newStack)) {
                inventory.setItem(slot, newStack);
                changed = true;
            }

            outputIndex++;
        }

        return changed;
    }

    private static final Comparator<ItemStack> STACK_COMPARATOR =
            Comparator
                    .comparingInt(BackpackSortHelper::category)
                    .thenComparingInt(BackpackSortHelper::registryId)
                    .thenComparingInt(BackpackSortHelper::damageSortValue)
                    .thenComparing(BackpackSortHelper::componentSortValue)
                    .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed());

    private static final Comparator<ItemStack> HOTBAR_STACK_COMPARATOR =
            Comparator
                    .comparingInt(BackpackSortHelper::hotbarCategory)
                    .thenComparingInt(BackpackSortHelper::registryId)
                    .thenComparingInt(BackpackSortHelper::damageSortValue)
                    .thenComparing(BackpackSortHelper::componentSortValue)
                    .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed());

    private static int category(ItemStack stack) {
        if (stack.isEmpty()) {
            return 999;
        }

        if (stack.has(DataComponents.TOOL)) {
            return 10;
        }

        if (stack.has(DataComponents.WEAPON)
                || stack.has(DataComponents.KINETIC_WEAPON)) {
            return 20;
        }

        if (stack.has(DataComponents.EQUIPPABLE)) {
            return 30;
        }

        if (stack.has(DataComponents.BLOCKS_ATTACKS)) {
            return 40;
        }

        if (stack.getItem() instanceof BlockItem) {
            return 50;
        }

        if (stack.has(DataComponents.FOOD)
                || stack.has(DataComponents.CONSUMABLE)) {
            return 60;
        }

        if (stack.isDamageableItem()) {
            return 70;
        }

        if (stack.getMaxStackSize() == 1) {
            return 80;
        }

        return 100;
    }

    private static int hotbarCategory(ItemStack stack) {
        if (stack.isEmpty()) {
            return 999;
        }


        if (stack.has(DataComponents.TOOL)
                || stack.has(DataComponents.WEAPON)
                || stack.has(DataComponents.KINETIC_WEAPON)
                || stack.has(DataComponents.EQUIPPABLE)
                || stack.has(DataComponents.BLOCKS_ATTACKS)
                || stack.isDamageableItem()
                || stack.getMaxStackSize() == 1) {
            return 10;
        }


        if (stack.has(DataComponents.FOOD)
                || stack.has(DataComponents.CONSUMABLE)) {
            return 20;
        }


        if (stack.getItem() instanceof BlockItem) {
            return 30;
        }

        return 100;
    }

    private static int registryId(ItemStack stack) {
        if (stack.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        return BuiltInRegistries.ITEM.getId(stack.getItem());
    }

    private static int damageSortValue(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            return 0;
        }

        return stack.getDamageValue();
    }

    private static String componentSortValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }


        return stack.getComponentsPatch().toString();
    }

    public static boolean sortAllSections(
            Container backpackInventory,
            BackpackTier tier,
            Container extraStorageInventory,
            boolean sortExtraStorage,
            Container playerInventory
    ) {
        boolean changed = false;

        changed |= sortContainerRange(
                backpackInventory,
                0,
                tier.protectedSlots,
                STACK_COMPARATOR
        );

        changed |= sortContainerRange(
                backpackInventory,
                tier.normalStart(),
                tier.upgradeStart(),
                STACK_COMPARATOR
        );

        if (sortExtraStorage && extraStorageInventory != null) {
            changed |= sortContainerRange(
                    extraStorageInventory,
                    0,
                    extraStorageInventory.getContainerSize(),
                    STACK_COMPARATOR
            );
        }

        changed |= sortContainerRange(
                playerInventory,
                9,
                36,
                STACK_COMPARATOR
        );

        changed |= sortContainerRange(
                playerInventory,
                0,
                9,
                HOTBAR_STACK_COMPARATOR
        );

        return changed;
    }
}