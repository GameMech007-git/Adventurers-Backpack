package com.anantaya.adventurersbackpack.upgrade.extrastorage;

import com.anantaya.adventurersbackpack.backpack.BackpackScreenHandler;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.DataSlot;

public final class ExtraStorageMenuHandler {

    private final BackpackScreenHandler menu;
    private final Container backpackInventory;
    private final Container extraStorageInventory;
    private final BackpackTier tier;

    private int syncedActive = 0;

    public ExtraStorageMenuHandler(
            BackpackScreenHandler menu,
            Container backpackInventory,
            Container extraStorageInventory,
            BackpackTier tier
    ) {
        this.menu = menu;
        this.backpackInventory = backpackInventory;
        this.extraStorageInventory = extraStorageInventory;
        this.tier = tier;
    }

    public void addDataSlots() {
        menu.addMenuDataSlot(new DataSlot() {
            @Override
            public int get() {
                return menu.hasUpgradeInstalled(
                        BackpackUpgradeItem.Type.EXTRA_STORAGE
                ) ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedActive = value;
            }
        });
    }

    public boolean hasUpgradeSynced() {
        return syncedActive == 1;
    }

    public boolean canUseSlots() {
        if (menu.hasUpgradeInstalled(
                BackpackUpgradeItem.Type.EXTRA_STORAGE
        )) {
            return true;
        }

        return hasUpgradeSynced();
    }

    public boolean hasAnyStoredItem() {
        if (extraStorageInventory == null) {
            return false;
        }

        for (int i = 0; i < extraStorageInventory.getContainerSize(); i++) {
            if (!extraStorageInventory.getItem(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public void setChanged() {
        if (extraStorageInventory != null) {
            extraStorageInventory.setChanged();
        }
    }
}