package com.anantaya.backpackpro.menu;

import com.anantaya.backpackpro.backpack.BackpackItem;
import com.anantaya.backpackpro.upgrade.BackpackUpgradeItem;

import net.minecraft.world.item.ItemStack;

public final class BackpackSlotRules {

    private BackpackSlotRules() {
    }

    public static boolean mayPlaceInMagicSlot(ItemStack stack) {
        return isNormalBackpackContent(stack);
    }

    public static boolean mayPlaceInNormalSlot(ItemStack stack) {
        return isNormalBackpackContent(stack);
    }

    public static boolean mayPlaceInUpgradeSlot(ItemStack stack) {
        return stack.getItem() instanceof BackpackUpgradeItem;
    }

    private static boolean isNormalBackpackContent(ItemStack stack) {
        return !(stack.getItem() instanceof BackpackItem)
                && !(stack.getItem() instanceof BackpackUpgradeItem);
    }
}