package com.anantaya.backpackpro.upgrade.fluidstorage;

import com.anantaya.backpackpro.upgrade.config.BackpackUpgradeDataHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class BackpackFluidStorageHelper {

    public static final int CAPACITY_BUCKETS = 8;

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

    public static boolean isFull(ItemStack backpackStack) {
        return getAmount(backpackStack) >= CAPACITY_BUCKETS;
    }

    public static boolean canInsert(
            ItemStack backpackStack,
            FluidStorageType insertedType
    ) {
        if (insertedType == FluidStorageType.NONE) {
            return false;
        }

        int amount = getAmount(backpackStack);

        if (amount >= CAPACITY_BUCKETS) {
            return false;
        }

        FluidStorageType currentType = getType(backpackStack);

        return amount <= 0
                || currentType == FluidStorageType.NONE
                || currentType == insertedType;
    }

    public static boolean insertOneBucket(
            ItemStack backpackStack,
            FluidStorageType insertedType
    ) {
        if (!canInsert(backpackStack, insertedType)) {
            return false;
        }

        int newAmount = getAmount(backpackStack) + 1;

        save(backpackStack, insertedType, newAmount);
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
            FluidStorageType requestedType
    ) {
        if (!canWithdraw(backpackStack, requestedType)) {
            return false;
        }

        int newAmount = getAmount(backpackStack) - 1;

        if (newAmount <= 0) {
            save(backpackStack, FluidStorageType.NONE, 0);
        } else {
            save(backpackStack, requestedType, newAmount);
        }

        return true;
    }

    public static void setFluid(
            ItemStack backpackStack,
            FluidStorageType type,
            int amount
    ) {
        if (backpackStack.isEmpty()) {
            return;
        }

        FluidStorageType savedType = type == null ? FluidStorageType.NONE : type;
        int clampedAmount = Math.max(0, Math.min(CAPACITY_BUCKETS, amount));

        if (clampedAmount <= 0) {
            savedType = FluidStorageType.NONE;
            clampedAmount = 0;
        }

        save(backpackStack, savedType, clampedAmount);
    }

    private static void save(
            ItemStack backpackStack,
            FluidStorageType type,
            int amount
    ) {
        CompoundTag tag = BackpackUpgradeDataHelper.getTag(backpackStack);

        int clampedAmount = Math.max(0, Math.min(CAPACITY_BUCKETS, amount));
        FluidStorageType savedType = clampedAmount <= 0 ? FluidStorageType.NONE : type;

        tag.putString(KEY_FLUID_TYPE, savedType.id());
        tag.putInt(KEY_FLUID_AMOUNT, clampedAmount);

        BackpackUpgradeDataHelper.saveTag(backpackStack, tag);
    }


}