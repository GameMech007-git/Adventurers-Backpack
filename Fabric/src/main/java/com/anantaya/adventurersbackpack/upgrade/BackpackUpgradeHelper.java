package com.anantaya.adventurersbackpack.upgrade;

import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.nested.NestedUpgradeData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public final class BackpackUpgradeHelper {

    private static final String INVENTORY_KEY = "Inventory";
    private static final String SLOT_PREFIX = "Slot";

    private BackpackUpgradeHelper() {
    }

    public static boolean hasUpgrade(
            ItemStack backpackStack,
            BackpackUpgradeItem.Type type,
            BackpackTier tier,
            RegistryAccess registryAccess
    ) {
        if (backpackStack.isEmpty()) {
            return false;
        }

        if (!(backpackStack.getItem() instanceof BackpackItem)) {
            return false;
        }

        CustomData customData = backpackStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        CompoundTag rootTag = customData.copyTag();

        Optional<CompoundTag> inventoryTagOptional = rootTag.getCompound(INVENTORY_KEY);
        if (inventoryTagOptional.isEmpty()) {
            return false;
        }

        CompoundTag inventoryTag = inventoryTagOptional.get();

        for (int i = 0; i < tier.upgradeSlots; i++) {
            int slotIndex = tier.upgradeStart() + i;
            String key = SLOT_PREFIX + slotIndex;

            Optional<CompoundTag> itemTagOptional = inventoryTag.getCompound(key);
            if (itemTagOptional.isEmpty()) {
                continue;
            }

            ItemStack upgradeStack = ItemStack.CODEC
                    .parse(
                            registryAccess.createSerializationContext(NbtOps.INSTANCE),
                            itemTagOptional.get()
                    )
                    .result()
                    .orElse(ItemStack.EMPTY);

            if (isUpgrade(upgradeStack, type)) {
                return true;
            }
        }

        return NestedUpgradeData.hasUpgrade(
                backpackStack,
                type,
                registryAccess
        );
    }

    public static boolean hasUpgrade(
            Container inventory,
            BackpackUpgradeItem.Type type,
            BackpackTier tier
    ) {
        for (int i = 0; i < tier.upgradeSlots; i++) {
            int slotIndex = tier.upgradeStart() + i;
            ItemStack stack = inventory.getItem(slotIndex);

            if (isUpgrade(stack, type)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isUpgrade(ItemStack stack, BackpackUpgradeItem.Type type) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!(stack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return false;
        }

        return upgradeItem.getType() == type;
    }
}