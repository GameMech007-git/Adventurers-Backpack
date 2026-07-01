package com.anantaya.adventurersbackpack.client.screen.panel;

import com.anantaya.adventurersbackpack.client.screen.panel.autopickup.AutoPickupConfigPanel;
import com.anantaya.adventurersbackpack.client.screen.panel.cartography.CartographersCaseConfigPanel;
import com.anantaya.adventurersbackpack.client.screen.panel.foodpouch.FoodPouchConfigPanel;
import com.anantaya.adventurersbackpack.client.screen.panel.recall.RecallRuneConfigPanel;
import com.anantaya.adventurersbackpack.client.screen.panel.restock.RestockConfigPanel;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.client.gui.BackpackGuiRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

public final class UpgradeConfigPanel {

    private static final int PANEL_W = 32;
    private static final int PANEL_H = 18;

    private UpgradeConfigPanel() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            ItemStack upgradeStack,
            ItemStack backpackStack,
            int syncedCartographerActiveSlot,
            HolderLookup.Provider registries,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ){
        if (!(upgradeStack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return;
        }

        if (upgradeItem.getType() == BackpackUpgradeItem.Type.RECALL_RUNE) {
            BackpackGuiRenderer.drawConfigPanel(
                    graphics,
                    panelX,
                    panelY,
                    RecallRuneConfigPanel.WIDTH,
                    RecallRuneConfigPanel.HEIGHT
            );

            RecallRuneConfigPanel.draw(
                    graphics,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY
            );

            return;
        }

        if (upgradeItem.getType() == BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE) {
            BackpackGuiRenderer.drawConfigPanel(
                    graphics,
                    panelX,
                    panelY,
                    CartographersCaseConfigPanel.WIDTH,
                    CartographersCaseConfigPanel.HEIGHT
            );

            CartographersCaseConfigPanel.draw(
                    graphics,
                    backpackStack,
                    registries,
                    syncedCartographerActiveSlot,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY
            );

            return;
        }

        if (upgradeItem.getType() == BackpackUpgradeItem.Type.AUTO_PICKUP) {
            BackpackGuiRenderer.drawConfigPanel(
                    graphics,
                    panelX,
                    panelY,
                    AutoPickupConfigPanel.WIDTH,
                    AutoPickupConfigPanel.HEIGHT
            );

            AutoPickupConfigPanel.draw(
                    graphics,
                    upgradeStack,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY
            );

            return;
        }

        BackpackGuiRenderer.drawConfigPanel(
                graphics,
                panelX,
                panelY,
                PANEL_W,
                PANEL_H
        );

        if (upgradeItem.getType() == BackpackUpgradeItem.Type.FOOD_POUCH) {
            FoodPouchConfigPanel.draw(
                    graphics,
                    upgradeStack,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY
            );
        } else if (upgradeItem.getType() == BackpackUpgradeItem.Type.RESTOCK) {
            RestockConfigPanel.draw(
                    graphics,
                    upgradeStack,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY
            );
        }
    }

    public static boolean mouseClicked(
            ItemStack upgradeStack,
            int upgradeSlotIndex,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY,
            boolean shiftDown,
            ActionSender sender
    ) {
        if (!(upgradeStack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return false;
        }

        return switch (upgradeItem.getType()) {
            case RECALL_RUNE -> RecallRuneConfigPanel.mouseClicked(
                    upgradeSlotIndex,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY,
                    shiftDown,
                    sender
            );

            case CARTOGRAPHERS_CASE -> CartographersCaseConfigPanel.mouseClicked(
                    upgradeSlotIndex,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY,
                    shiftDown
            );

            case AUTO_PICKUP -> AutoPickupConfigPanel.mouseClicked(
                    upgradeSlotIndex,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY,
                    sender
            );

            case FOOD_POUCH -> FoodPouchConfigPanel.mouseClicked(
                    upgradeSlotIndex,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY,
                    sender
            );

            case RESTOCK -> RestockConfigPanel.mouseClicked(
                    upgradeSlotIndex,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY,
                    sender
            );

            default -> false;
        };
    }

    @FunctionalInterface
    public interface ActionSender {
        void send(int upgradeSlotIndex, BackpackUpgradeConfigAction action);
    }
}