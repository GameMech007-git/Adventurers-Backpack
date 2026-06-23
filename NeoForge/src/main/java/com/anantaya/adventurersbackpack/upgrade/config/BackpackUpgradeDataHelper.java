package com.anantaya.adventurersbackpack.upgrade.config;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class BackpackUpgradeDataHelper {

    private BackpackUpgradeDataHelper() {
    }

    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        );

        return data.copyTag();
    }

    public static void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}