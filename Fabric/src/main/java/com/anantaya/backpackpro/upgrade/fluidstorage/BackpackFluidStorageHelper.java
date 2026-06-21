package com.anantaya.backpackpro.upgrade.fluidstorage;

import com.anantaya.backpackpro.backpack.BackpackTier;
import com.anantaya.backpackpro.upgrade.config.BackpackUpgradeDataHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class BackpackFluidStorageHelper {

    public static final int IRON_CAPACITY_BUCKETS = 8;
    public static final int DIAMOND_CAPACITY_BUCKETS = 12;
    public static final int NETHERITE_CAPACITY_BUCKETS = 16;

    // Keep this only for old/simple calls. Treat it as iron/default capacity.
    public static final int CAPACITY_BUCKETS = IRON_CAPACITY_BUCKETS;

    public static int capacityForTier(BackpackTier tier) {
        return switch (tier) {
            case IRON -> IRON_CAPACITY_BUCKETS;
            case DIAMOND -> DIAMOND_CAPACITY_BUCKETS;
            case NETHERITE -> NETHERITE_CAPACITY_BUCKETS;
        };
    }

    private static final String KEY_FLUID_TYPE = "BackpackProFluidType";
    private static final String KEY_FLUID_AMOUNT = "BackpackProFluidAmount";

    private BackpackFluidStorageHelper() {
    }

    public static FluidStorageType getType(ItemStack backpackStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(backpackStack);

        if (!tag.contains(KEY_FLUID_TYPE)) {
            return FluidStorageType.NONE;
        }

        return FluidStorageType.byId(tag.getString(KEY_FLUID_TYPE).orElse("none"));
    }

    public static int getAmount(ItemStack backpackStack) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(backpackStack);

        if (!tag.contains(KEY_FLUID_AMOUNT)) {
            return 0;
        }

        return Math.max(0, tag.getInt(KEY_FLUID_AMOUNT).orElse(0));
    }

    public static boolean isEmpty(ItemStack backpackStack) {
        return getAmount(backpackStack) <= 0 || getType(backpackStack) == FluidStorageType.NONE;
    }

    public static boolean isFull(
            ItemStack backpackStack,
            BackpackTier tier
    ) {
        return getAmount(backpackStack) >= capacityForTier(tier);
    }

    public static boolean canInsert(
            ItemStack backpackStack,
            FluidStorageType insertedType,
            BackpackTier tier
    ) {
        if (insertedType == FluidStorageType.NONE) {
            return false;
        }

        int amount = getAmount(backpackStack);

        if (amount >= capacityForTier(tier)) {
            return false;
        }

        FluidStorageType currentType = getType(backpackStack);

        return amount <= 0
                || currentType == FluidStorageType.NONE
                || currentType == insertedType;
    }

    public static boolean insertOneBucket(
            ItemStack backpackStack,
            FluidStorageType insertedType,
            BackpackTier tier
    ) {
        if (!canInsert(backpackStack, insertedType, tier)) {
            return false;
        }

        int newAmount = getAmount(backpackStack) + 1;

        save(backpackStack, insertedType, newAmount, tier);
        return true;
    }

    public static boolean canWithdraw(
            ItemStack backpackStack,
            FluidStorageType requestedType
    ) {
        if (requestedType == FluidStorageType.NONE) {
            return false;
        }

        int amount = getAmount(backpackStack);

        if (amount <= 0) {
            return false;
        }

        return getType(backpackStack) == requestedType;
    }

    public static boolean withdrawOneBucket(
            ItemStack backpackStack,
            FluidStorageType requestedType,
            BackpackTier tier
    ) {
        if (!canWithdraw(backpackStack, requestedType)) {
            return false;
        }

        int newAmount = getAmount(backpackStack) - 1;

        if (newAmount <= 0) {
            save(backpackStack, FluidStorageType.NONE, 0, tier);
        } else {
            save(backpackStack, requestedType, newAmount, tier);
        }

        return true;
    }

    public static void setFluid(
            ItemStack backpackStack,
            FluidStorageType type,
            int amount,
            BackpackTier tier
    ) {
        if (backpackStack.isEmpty()) {
            return;
        }

        FluidStorageType savedType = type == null ? FluidStorageType.NONE : type;
        int clampedAmount = Math.max(0, Math.min(capacityForTier(tier), amount));

        if (clampedAmount <= 0) {
            savedType = FluidStorageType.NONE;
            clampedAmount = 0;
        }

        save(backpackStack, savedType, clampedAmount, tier);
    }

    private static void save(
            ItemStack backpackStack,
            FluidStorageType type,
            int amount,
            BackpackTier tier
    ) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(backpackStack);

        int clampedAmount = Math.max(0, Math.min(capacityForTier(tier), amount));
        FluidStorageType savedType = clampedAmount <= 0 ? FluidStorageType.NONE : type;

        tag.putString(KEY_FLUID_TYPE, savedType.id());
        tag.putInt(KEY_FLUID_AMOUNT, clampedAmount);

        BackpackUpgradeDataHelper.saveTag(backpackStack, tag);
    }


}