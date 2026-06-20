package com.anantaya.backpackpro.upgrade.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class RestockConfig {

    private static final String KEY_TOOLS = "BackpackProRestockTools";
    private static final String KEY_BUCKETS = "BackpackProRestockBuckets";

    private RestockConfig() {
    }

    public static boolean shouldRestockTools(ItemStack upgradeStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);

        return tag.getBoolean(KEY_TOOLS).orElse(true);
    }

    public static void toggleTools(ItemStack upgradeStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);
        tag.putBoolean(KEY_TOOLS, !shouldRestockTools(upgradeStack));
        BackpackUpgradeDataHelper.saveTag(upgradeStack, tag);
    }

    public static boolean shouldRestockBuckets(ItemStack upgradeStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);

        return tag.getBoolean(KEY_BUCKETS).orElse(true);
    }

    public static void toggleBuckets(ItemStack upgradeStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);
        tag.putBoolean(KEY_BUCKETS, !shouldRestockBuckets(upgradeStack));
        BackpackUpgradeDataHelper.saveTag(upgradeStack, tag);
    }
}