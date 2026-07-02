package com.anantaya.adventurersbackpack.client;

import com.anantaya.adventurersbackpack.client.screen.panel.UpgradeConfigPanel;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BackpackScreenUpgradePanel {

    private int selectedUpgradeSlot = -1;

    public void toggleSelectedUpgradeSlot(int slotIndex) {
        if (selectedUpgradeSlot == slotIndex) {
            selectedUpgradeSlot = -1;
        } else {
            selectedUpgradeSlot = slotIndex;
        }
    }

    public void draw(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        Slot slot = getSelectedUpgradeSlot(screen);

        if (slot == null || !slot.hasItem()) {
            return;
        }

        ItemStack stack = slot.getItem();

        if (!(stack.getItem() instanceof BackpackUpgradeItem)) {
            return;
        }

        HolderLookup.Provider registries = screen.minecraftClient().level != null
                ? screen.minecraftClient().level.registryAccess()
                : null;

        UpgradeConfigPanel.draw(
                graphics,
                stack,
                screen.handler().getBackpackStackForClient(),
                screen.handler().getSyncedCartographerActiveSlot(),
                registries,
                getConfigPanelX(screen),
                getConfigPanelY(screen, slot),
                mouseX,
                mouseY
        );
    }

    public boolean isSelectedUpgradeSlot(int slotIndex) {
        return selectedUpgradeSlot == slotIndex;
    }

    public boolean mouseClicked(
            BackpackScreen screen,
            int mouseX,
            int mouseY,
            boolean shiftDown
    ) {
        Slot slot = getSelectedUpgradeSlot(screen);

        if (slot == null || !slot.hasItem()) {
            return false;
        }

        ItemStack stack = slot.getItem();

        boolean handled = UpgradeConfigPanel.mouseClicked(
                stack,
                selectedUpgradeSlot,
                getConfigPanelX(screen),
                getConfigPanelY(screen, slot),
                mouseX,
                mouseY,
                shiftDown,
                screen::sendConfigAction
        );

        if (handled
                && stack.getItem() instanceof BackpackUpgradeItem upgradeItem
                && upgradeItem.getType() == BackpackUpgradeItem.Type.RECALL_RUNE) {
            screen.closeAfterRecallAction();
        }

        return handled;
    }

    private Slot getSelectedUpgradeSlot(BackpackScreen screen) {
        if (selectedUpgradeSlot < screen.handler().tier.upgradeStart()
                || selectedUpgradeSlot >= screen.handler().tier.totalSlots
                || selectedUpgradeSlot >= screen.handler().slots.size()) {
            return null;
        }

        return screen.handler().slots.get(selectedUpgradeSlot);
    }

    private int getConfigPanelX(BackpackScreen screen) {
        return screen.screenLeft()
                + screen.layout().upgradeSlotX()
                + 31;
    }

    private int getConfigPanelY(
            BackpackScreen screen,
            Slot slot
    ) {
        ItemStack stack = slot.getItem();

        if (stack.getItem() instanceof BackpackUpgradeItem upgradeItem
                && (upgradeItem.getType() == BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE
                || upgradeItem.getType() == BackpackUpgradeItem.Type.NESTED_UPGRADE)) {
            return screen.screenTop() + screen.layout().upgradeSlotY(0);
        }

        return screen.screenTop() + slot.y;
    }

    public boolean isCartographersCasePanelOpen(BackpackScreen screen) {
        Slot slot = getSelectedUpgradeSlot(screen);

        if (slot == null || !slot.hasItem()) {
            return false;
        }

        ItemStack stack = slot.getItem();

        return stack.getItem() instanceof BackpackUpgradeItem upgradeItem
                && upgradeItem.getType() == BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE;
    }

    public boolean isInsideSelectedPanel(
            BackpackScreen screen,
            double mouseX,
            double mouseY
    ) {
        Slot slot = getSelectedUpgradeSlot(screen);

        if (slot == null || !slot.hasItem()) {
            return false;
        }

        ItemStack stack = slot.getItem();

        int panelW = 32;
        int panelH = 18;

        if (stack.getItem() instanceof BackpackUpgradeItem upgradeItem) {
            if (upgradeItem.getType() == BackpackUpgradeItem.Type.RECALL_RUNE) {
                panelW = 44;
            } else if (upgradeItem.getType() == BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE) {
                panelW = 64;
                panelH = 74;
            } else if (upgradeItem.getType() == BackpackUpgradeItem.Type.NESTED_UPGRADE) {
                panelW = 48;
                panelH = 48;
            }
        }

        int panelX = getConfigPanelX(screen);
        int panelY = getConfigPanelY(screen, slot);

        return mouseX >= panelX
                && mouseX < panelX + panelW
                && mouseY >= panelY
                && mouseY < panelY + panelH;
    }

    public static boolean isConfigurableUpgradeSlot(Slot slot) {
        if (!slot.hasItem()) {
            return false;
        }

        ItemStack stack = slot.getItem();

        if (!(stack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return false;
        }

        return upgradeItem.getType() == BackpackUpgradeItem.Type.AUTO_PICKUP
                || upgradeItem.getType() == BackpackUpgradeItem.Type.FOOD_POUCH
                || upgradeItem.getType() == BackpackUpgradeItem.Type.RESTOCK
                || upgradeItem.getType() == BackpackUpgradeItem.Type.RECALL_RUNE
                || upgradeItem.getType() == BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE
                || upgradeItem.getType() == BackpackUpgradeItem.Type.NESTED_UPGRADE;
    }


}