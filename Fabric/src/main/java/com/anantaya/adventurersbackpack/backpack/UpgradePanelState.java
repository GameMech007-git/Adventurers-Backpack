package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class UpgradePanelState {

    private BackpackUpgradeItem.Type openType = null;
    private int openSlotIndex = -1;

    public boolean isOpen(
            BackpackUpgradeItem.Type type,
            List<Slot> slots
    ) {
        if (openType != type) {
            return false;
        }

        if (!isStillValid(type, slots)) {
            close();
            return false;
        }

        return true;
    }

    public boolean isOpenForSlot(
            BackpackUpgradeItem.Type type,
            int slotIndex,
            List<Slot> slots
    ) {
        return isOpen(type, slots)
                && openSlotIndex == slotIndex;
    }

    public void open(
            BackpackUpgradeItem.Type type,
            int slotIndex
    ) {
        this.openType = type;
        this.openSlotIndex = slotIndex;
    }

    public void close() {
        this.openType = null;
        this.openSlotIndex = -1;
    }

    public BackpackUpgradeItem.Type openType() {
        return openType;
    }

    public int openSlotIndex() {
        return openSlotIndex;
    }

    private boolean isStillValid(
            BackpackUpgradeItem.Type type,
            List<Slot> slots
    ) {
        if (openSlotIndex < 0 || openSlotIndex >= slots.size()) {
            return false;
        }

        Slot slot = slots.get(openSlotIndex);

        if (!slot.hasItem()) {
            return false;
        }

        ItemStack stack = slot.getItem();

        return stack.getItem() instanceof BackpackUpgradeItem upgradeItem
                && upgradeItem.getType() == type;
    }
}