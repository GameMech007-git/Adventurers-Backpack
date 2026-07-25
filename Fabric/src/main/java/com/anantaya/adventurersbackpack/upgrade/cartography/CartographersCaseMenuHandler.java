package com.anantaya.adventurersbackpack.upgrade.cartography;

import com.anantaya.adventurersbackpack.backpack.BackpackScreenHandler;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class CartographersCaseMenuHandler {

    private final BackpackScreenHandler menu;
    private final Container backpackInventory;
    private final Container cartographersCaseInventory;
    private final ItemStack backpackStack;
    private final BackpackTier tier;

    private int syncedActiveSlot = CartographersCaseHelper.NO_ACTIVE_SLOT;

    public CartographersCaseMenuHandler(
            BackpackScreenHandler menu,
            Container backpackInventory,
            Container cartographersCaseInventory,
            ItemStack backpackStack,
            BackpackTier tier
    ) {
        this.menu = menu;
        this.backpackInventory = backpackInventory;
        this.cartographersCaseInventory = cartographersCaseInventory;
        this.backpackStack = backpackStack;
        this.tier = tier;
    }

    public void addDataSlots() {
        menu.addMenuDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (backpackStack.isEmpty()) {
                    return CartographersCaseHelper.NO_ACTIVE_SLOT;
                }

                return CartographersCaseData.getActiveSlot(backpackStack);
            }

            @Override
            public void set(int value) {
                syncedActiveSlot = value;
            }
        });
    }

    public int getSyncedActiveSlot() {
        return syncedActiveSlot;
    }

    public boolean hasUpgrade() {
        return BackpackUpgradeHelper.hasUpgrade(
                backpackInventory,
                BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE,
                tier
        );
    }

    public boolean hasAnyStoredItem() {
        if (cartographersCaseInventory == null) {
            return false;
        }

        for (int i = 0; i < cartographersCaseInventory.getContainerSize(); i++) {
            if (!cartographersCaseInventory.getItem(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public void saveToBackpack() {
        if (cartographersCaseInventory instanceof CartographersCaseInventory cartographersCase) {
            cartographersCase.saveToBackpack();
        }
    }

    public void handleClick(
            Player player,
            int upgradeSlotIndex,
            int navigationSlot
    ) {
        if (backpackStack.isEmpty()) {
            return;
        }

        if (upgradeSlotIndex < 0 || upgradeSlotIndex >= menu.slots.size()) {
            return;
        }

        Slot upgradeSlot = menu.slots.get(upgradeSlotIndex);
        ItemStack upgradeStack = upgradeSlot.getItem();

        if (!BackpackUpgradeHelper.isUpgrade(
                upgradeStack,
                BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE
        )) {
            return;
        }

        if (navigationSlot == CartographersCaseHelper.DEATH_SIGNAL_SLOT) {
            toggleActiveSlot(player, CartographersCaseHelper.DEATH_SIGNAL_SLOT);
            return;
        }

        if (cartographersCaseInventory == null) {
            return;
        }

        if (!CartographersCaseHelper.isCompassSlotIndex(navigationSlot)) {
            return;
        }

        int compassInventoryIndex =
                CartographersCaseHelper.toCompassInventoryIndex(navigationSlot);

        saveToBackpack();

        ItemStack compass = cartographersCaseInventory.getItem(compassInventoryIndex);

        if (compass.isEmpty()) {
            return;
        }

        if (!CartographersCaseHelper.isValidLodestoneCompass(compass)) {
            return;
        }

        toggleActiveSlot(player, navigationSlot);
    }

    private void toggleActiveSlot(
            Player player,
            int navigationSlot
    ) {
        int activeSlot = CartographersCaseData.getActiveSlot(backpackStack);

        CartographersCaseData.setActiveSlot(
                backpackStack,
                activeSlot == navigationSlot
                        ? CartographersCaseHelper.NO_ACTIVE_SLOT
                        : navigationSlot
        );

        sync(player);
    }

    private void sync(Player player) {
        saveToBackpack();

        backpackInventory.setChanged();

        if (player != null) {
            player.getInventory().setChanged();
        }

        menu.broadcastChanges();
    }
}