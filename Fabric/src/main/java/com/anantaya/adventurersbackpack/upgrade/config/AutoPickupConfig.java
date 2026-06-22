package com.anantaya.adventurersbackpack.upgrade.config;

import com.anantaya.adventurersbackpack.upgrade.autopickup.AutoPickupMode;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class AutoPickupConfig {

    private static final String MODE_KEY = "AutoPickupMode";
    private static final String IGNORE_PLAYER_DROPS_KEY = "IgnorePlayerDrops";

    private AutoPickupConfig() {
    }

    public static AutoPickupMode getMode(ItemStack upgradeStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);

        String value = tag.getString(MODE_KEY).orElse("MATCHING_ONLY");

        try {
            return AutoPickupMode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return AutoPickupMode.MATCHING_ONLY;
        }
    }

    public static void cycleMode(ItemStack upgradeStack) {
        AutoPickupMode current = getMode(upgradeStack);
        setMode(upgradeStack, current.next());
    }

    public static void setMode(ItemStack upgradeStack, AutoPickupMode mode) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);
        tag.putString(MODE_KEY, mode.name());
        BackpackUpgradeDataHelper.saveTag(upgradeStack, tag);
    }

    public static boolean shouldIgnorePlayerDrops(ItemStack upgradeStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);

        if (!tag.contains(IGNORE_PLAYER_DROPS_KEY)) {
            return true;
        }

        return tag.getBoolean(IGNORE_PLAYER_DROPS_KEY).orElse(true);
    }

    public static void toggleIgnorePlayerDrops(ItemStack upgradeStack) {
        setIgnorePlayerDrops(
                upgradeStack,
                !shouldIgnorePlayerDrops(upgradeStack)
        );
    }

    public static void setIgnorePlayerDrops(ItemStack upgradeStack, boolean value) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(upgradeStack);
        tag.putBoolean(IGNORE_PLAYER_DROPS_KEY, value);
        BackpackUpgradeDataHelper.saveTag(upgradeStack, tag);
    }
}