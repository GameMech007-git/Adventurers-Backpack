package com.anantaya.adventurersbackpack.upgrade.config;

import com.anantaya.adventurersbackpack.upgrade.foodpouch.FoodPouchMode;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class FoodPouchConfig {

    private static final String MODE_KEY = "FoodPouchMode";
    private static final String EMPTY_WARNING_KEY = "FoodPouchEmptyWarning";

    private FoodPouchConfig() {
    }

    public static FoodPouchMode getMode(ItemStack upgradeStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);

        String value = tag.getString(MODE_KEY).orElse("SMART");

        try {
            return FoodPouchMode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return FoodPouchMode.SMART;
        }
    }

    public static void cycleMode(ItemStack upgradeStack) {
        FoodPouchMode current = getMode(upgradeStack);
        setMode(upgradeStack, current.next());
    }

    public static void setMode(ItemStack upgradeStack, FoodPouchMode mode) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);
        tag.putString(MODE_KEY, mode.name());
        BackpackUpgradeDataHelper.saveTag(upgradeStack, tag);
    }

    public static boolean shouldWarnWhenEmpty(ItemStack upgradeStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);

        if (!tag.contains(EMPTY_WARNING_KEY)) {
            return true;
        }

        return tag.getBoolean(EMPTY_WARNING_KEY).orElse(true);
    }

    public static void toggleEmptyWarning(ItemStack upgradeStack) {
        setEmptyWarning(
                upgradeStack,
                !shouldWarnWhenEmpty(upgradeStack)
        );
    }

    public static void setEmptyWarning(ItemStack upgradeStack, boolean value) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);
        tag.putBoolean(EMPTY_WARNING_KEY, value);
        BackpackUpgradeDataHelper.saveTag(upgradeStack, tag);
    }
}