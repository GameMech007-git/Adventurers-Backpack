package com.anantaya.adventurersbackpack.upgrade.cartography;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public final class CartographersCaseInventory extends SimpleContainer {

    private final ItemStack backpackStack;
    private final HolderLookup.Provider registries;
    private boolean loading = false;

    public CartographersCaseInventory(
            ItemStack backpackStack,
            HolderLookup.Provider registries
    ) {
        super(CartographersCaseHelper.LODESTONE_COMPASS_SLOT_COUNT);

        this.backpackStack = backpackStack;
        this.registries = registries;

        loadFromBackpack();
    }

    private void loadFromBackpack() {
        if (backpackStack.isEmpty()) {
            return;
        }

        loading = true;

        for (int i = 0; i < getContainerSize(); i++) {
            super.setItem(
                    i,
                    CartographersCaseData.getCompass(
                            backpackStack,
                            i,
                            registries
                    )
            );
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

        for (int i = 0; i < getContainerSize(); i++) {
            CartographersCaseData.setCompass(
                    backpackStack,
                    i,
                    getItem(i),
                    registries
            );
        }

        clearActiveSlotIfCompassWasRemoved();
    }

    private void clearActiveSlotIfCompassWasRemoved() {
        int activeSlot = CartographersCaseData.getActiveSlot(backpackStack);

        if (!CartographersCaseHelper.isCompassSlotIndex(activeSlot)) {
            return;
        }

        int compassInventoryIndex =
                CartographersCaseHelper.toCompassInventoryIndex(activeSlot);

        ItemStack activeCompass = getItem(compassInventoryIndex);

        if (activeCompass.isEmpty()
                || !CartographersCaseHelper.isValidLodestoneCompass(activeCompass)) {
            CartographersCaseData.setActiveSlot(
                    backpackStack,
                    CartographersCaseHelper.NO_ACTIVE_SLOT
            );
        }
    }
}