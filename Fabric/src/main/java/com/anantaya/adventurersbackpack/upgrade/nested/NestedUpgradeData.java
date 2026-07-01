package com.anantaya.adventurersbackpack.upgrade.nested;

import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public final class NestedUpgradeData {

    public static final String KEY = "NestedUpgrade";
    public static final String SLOT_PREFIX = "Slot";
    public static final int SLOT_COUNT = 4;

    private NestedUpgradeData() {
    }

    public static boolean hasUpgrade(
            ItemStack backpackStack,
            BackpackUpgradeItem.Type type,
            RegistryAccess registryAccess
    ) {
        if (!type.canBeNested()) {
            return false;
        }

        if (backpackStack.isEmpty()) {
            return false;
        }

        CustomData customData = backpackStack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return false;
        }

        CompoundTag rootTag = customData.copyTag();

        Optional<CompoundTag> nestedTagOptional = rootTag.getCompound(KEY);

        if (nestedTagOptional.isEmpty()) {
            return false;
        }

        CompoundTag nestedTag = nestedTagOptional.get();

        for (int i = 0; i < SLOT_COUNT; i++) {
            Optional<CompoundTag> itemTagOptional =
                    nestedTag.getCompound(SLOT_PREFIX + i);

            if (itemTagOptional.isEmpty()) {
                continue;
            }

            ItemStack nestedStack = ItemStack.CODEC
                    .parse(
                            registryAccess.createSerializationContext(NbtOps.INSTANCE),
                            itemTagOptional.get()
                    )
                    .result()
                    .orElse(ItemStack.EMPTY);

            if (isNestedUpgrade(nestedStack, type)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasUpgrade(
            Container nestedInventory,
            BackpackUpgradeItem.Type type
    ) {
        if (!type.canBeNested()) {
            return false;
        }

        for (int i = 0; i < nestedInventory.getContainerSize(); i++) {
            if (isNestedUpgrade(nestedInventory.getItem(i), type)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAllowedNestedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        if (!(stack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return false;
        }

        return upgradeItem.getType().canBeNested();
    }

    private static boolean isNestedUpgrade(
            ItemStack stack,
            BackpackUpgradeItem.Type type
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!(stack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return false;
        }

        return upgradeItem.getType() == type
                && upgradeItem.getType().canBeNested();
    }
}