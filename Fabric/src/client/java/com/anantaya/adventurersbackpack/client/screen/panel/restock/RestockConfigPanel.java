package com.anantaya.adventurersbackpack.client.screen.panel.restock;

import com.anantaya.adventurersbackpack.client.gui.BackpackGuiRenderer;
import com.anantaya.adventurersbackpack.client.screen.panel.UpgradeConfigPanel;
import com.anantaya.adventurersbackpack.client.screen.panel.UpgradePanelUtil;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import com.anantaya.adventurersbackpack.upgrade.config.RestockConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class RestockConfigPanel {

    public static final int WIDTH = 32;
    public static final int HEIGHT = 18;

    private static final int MODE_BUTTON_X = 5;
    private static final int MODE_BUTTON_Y = 4;
    private static final int MODE_BUTTON_W = 10;
    private static final int MODE_BUTTON_H = 10;

    private static final int SECOND_BUTTON_X = 17;
    private static final int SECOND_BUTTON_Y = 4;
    private static final int SECOND_BUTTON_W = 10;
    private static final int SECOND_BUTTON_H = 10;

    private static final Identifier RESTOCK_BUCKETS_ON =
            UpgradePanelUtil.texture("config/restock_buckets_on");

    private static final Identifier RESTOCK_BUCKETS_OFF =
            UpgradePanelUtil.texture("config/restock_buckets_off");

    private static final Identifier RESTOCK_TOOLS_ON =
            UpgradePanelUtil.texture("config/restock_tools_on");

    private static final Identifier RESTOCK_TOOLS_OFF =
            UpgradePanelUtil.texture("config/restock_tools_off");

    private RestockConfigPanel() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            ItemStack upgradeStack,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        boolean tools = RestockConfig.shouldRestockTools(upgradeStack);
        boolean buckets = RestockConfig.shouldRestockBuckets(upgradeStack);

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                tools ? RESTOCK_TOOLS_ON : RESTOCK_TOOLS_OFF,
                panelX + MODE_BUTTON_X,
                panelY + MODE_BUTTON_Y,
                MODE_BUTTON_W,
                MODE_BUTTON_H
        );

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                buckets ? RESTOCK_BUCKETS_ON : RESTOCK_BUCKETS_OFF,
                panelX + SECOND_BUTTON_X,
                panelY + SECOND_BUTTON_Y,
                SECOND_BUTTON_W,
                SECOND_BUTTON_H
        );

        drawTooltip(
                graphics,
                mouseX,
                mouseY,
                panelX,
                panelY,
                tools,
                buckets
        );
    }

    public static boolean mouseClicked(
            int upgradeSlotIndex,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY,
            UpgradeConfigPanel.ActionSender sender
    ) {
        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + MODE_BUTTON_X,
                panelY + MODE_BUTTON_Y,
                MODE_BUTTON_W,
                MODE_BUTTON_H
        )) {
            sender.send(
                    upgradeSlotIndex,
                    BackpackUpgradeConfigAction.TOGGLE_RESTOCK_TOOLS
            );
            return true;
        }

        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + SECOND_BUTTON_X,
                panelY + SECOND_BUTTON_Y,
                SECOND_BUTTON_W,
                SECOND_BUTTON_H
        )) {
            sender.send(
                    upgradeSlotIndex,
                    BackpackUpgradeConfigAction.TOGGLE_RESTOCK_BUCKETS
            );
            return true;
        }

        return false;
    }

    private static void drawTooltip(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY,
            boolean tools,
            boolean buckets
    ) {
        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + MODE_BUTTON_X,
                panelY + MODE_BUTTON_Y,
                MODE_BUTTON_W,
                MODE_BUTTON_H
        )) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Restock tools: " + (tools ? "On" : "Off")),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + SECOND_BUTTON_X,
                panelY + SECOND_BUTTON_Y,
                SECOND_BUTTON_W,
                SECOND_BUTTON_H
        )) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Restock buckets: " + (buckets ? "On" : "Off")),
                    mouseX,
                    mouseY
            );
        }
    }
}