package com.anantaya.adventurersbackpack.upgrade.nested;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class NestedUpgradeInventory extends SimpleContainer {

    private final ItemStack backpackStack;
    private final HolderLookup.Provider registries;
    private boolean loading = false;

    public NestedUpgradeInventory(
            ItemStack backpackStack,
            HolderLookup.Provider registries
    ) {
        super(NestedUpgradeData.SLOT_COUNT);

        this.backpackStack = backpackStack;
        this.registries = registries;

        loadFromBackpack();
    }

    private void loadFromBackpack() {
        if (backpackStack.isEmpty()) {
            return;
        }

        CustomData customData = backpackStack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return;
        }

        CompoundTag rootTag = customData.copyTag();

        CompoundTag nestedTag = rootTag.getCompound(NestedUpgradeData.KEY)
                .orElse(new CompoundTag());

        loading = true;

        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = nestedTag.getCompound(NestedUpgradeData.SLOT_PREFIX + i)
                    .flatMap(itemTag -> ItemStack.CODEC
                            .parse(
                                    registries.createSerializationContext(NbtOps.INSTANCE),
                                    itemTag
                            )
                            .result()
                    )
                    .orElse(ItemStack.EMPTY);

            super.setItem(i, stack);
        }

        loading = false;
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (loading || backpackStack.isEmpty()) {
            return;
        }

        saveToBackpack();
    }

    public void saveToBackpack() {
        if (backpackStack.isEmpty()) {
            return;
        }

        CustomData customData = backpackStack.get(DataComponents.CUSTOM_DATA);
        CompoundTag rootTag = customData == null
                ? new CompoundTag()
                : customData.copyTag();

        CompoundTag nestedTag = new CompoundTag();

        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (!NestedUpgradeData.isAllowedNestedStack(stack)) {
                continue;
            }

            ItemStack copy = stack.copy();
            copy.setCount(1);

            final String key = NestedUpgradeData.SLOT_PREFIX + i;

            ItemStack.CODEC
                    .encodeStart(
                            registries.createSerializationContext(NbtOps.INSTANCE),
                            copy
                    )
                    .result()
                    .ifPresent(tag -> nestedTag.put(key, tag));
        }

        if (nestedTag.isEmpty()) {
            rootTag.remove(NestedUpgradeData.KEY);
        } else {
            rootTag.put(NestedUpgradeData.KEY, nestedTag);
        }

        backpackStack.set(DataComponents.CUSTOM_DATA, CustomData.of(rootTag));
    }
}