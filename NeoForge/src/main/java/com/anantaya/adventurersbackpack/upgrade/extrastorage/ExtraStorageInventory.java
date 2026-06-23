package com.anantaya.adventurersbackpack.upgrade.extrastorage;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public class ExtraStorageInventory extends SimpleContainer {

    private static final String EXTRA_STORAGE_KEY = "ExtraStorage";
    private static final String SLOT_PREFIX = "Slot";

    private final ItemStack backpackStack;
    private final RegistryAccess registryAccess;

    public ExtraStorageInventory(
            ItemStack backpackStack,
            int size,
            RegistryAccess registryAccess
    ) {
        super(size);

        this.backpackStack = backpackStack;
        this.registryAccess = registryAccess;

        loadFromData();
    }

    public void loadFromData() {
        if (backpackStack.isEmpty()) {
            return;
        }

        CustomData customData = backpackStack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return;
        }

        CompoundTag rootTag = customData.copyTag();

        Optional<CompoundTag> extraTagOptional =
                rootTag.getCompound(EXTRA_STORAGE_KEY);

        if (extraTagOptional.isEmpty()) {
            return;
        }

        CompoundTag extraTag = extraTagOptional.get();

        for (int i = 0; i < getContainerSize(); i++) {
            String key = SLOT_PREFIX + i;

            Optional<CompoundTag> itemTagOptional = extraTag.getCompound(key);

            if (itemTagOptional.isEmpty()) {
                continue;
            }

            ItemStack item = ItemStack.CODEC
                    .parse(
                            registryAccess.createSerializationContext(NbtOps.INSTANCE),
                            itemTagOptional.get()
                    )
                    .result()
                    .orElse(ItemStack.EMPTY);

            setItem(i, item);
        }
    }

    public void saveToData() {
        if (backpackStack.isEmpty()) {
            return;
        }

        CustomData customData = backpackStack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        );

        CompoundTag rootTag = customData.copyTag();
        CompoundTag extraTag = new CompoundTag();

        for (int i = 0; i < getContainerSize(); i++) {
            final int slot = i;
            ItemStack item = getItem(i);

            if (item.isEmpty()) {
                continue;
            }

            ItemStack.CODEC
                    .encodeStart(
                            registryAccess.createSerializationContext(NbtOps.INSTANCE),
                            item
                    )
                    .result()
                    .ifPresent(tagData -> extraTag.put(
                            SLOT_PREFIX + slot,
                            tagData
                    ));
        }

        rootTag.put(EXTRA_STORAGE_KEY, extraTag);
        backpackStack.set(DataComponents.CUSTOM_DATA, CustomData.of(rootTag));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        saveToData();
    }
}