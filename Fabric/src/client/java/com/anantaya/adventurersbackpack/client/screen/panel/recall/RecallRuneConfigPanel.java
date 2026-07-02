package com.anantaya.adventurersbackpack.client.screen.panel.recall;

import com.anantaya.adventurersbackpack.client.gui.BackpackGuiRenderer;
import com.anantaya.adventurersbackpack.client.screen.panel.UpgradeConfigPanel;
import com.anantaya.adventurersbackpack.client.screen.panel.UpgradePanelUtil;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class RecallRuneConfigPanel {

    public static final int WIDTH = 44;
    public static final int HEIGHT = 18;

    private static final int HOME_X = 5;
    private static final int WAYPOINT_1_X = 17;
    private static final int WAYPOINT_2_X = 29;

    private static final int BUTTON_Y = 4;
    private static final int BUTTON_W = 10;
    private static final int BUTTON_H = 10;

    private static final Identifier RECALL_HOME =
            UpgradePanelUtil.texture("config/recall_home");

    private static final Identifier RECALL_WAYPOINT_1 =
            UpgradePanelUtil.texture("config/recall_waypoint_1");

    private static final Identifier RECALL_WAYPOINT_2 =
            UpgradePanelUtil.texture("config/recall_waypoint_2");

    private RecallRuneConfigPanel() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        BackpackGuiRenderer.drawTextureButton(
                graphics,
                RECALL_HOME,
                panelX + HOME_X,
                panelY + BUTTON_Y,
                BUTTON_W,
                BUTTON_H
        );

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                RECALL_WAYPOINT_1,
                panelX + WAYPOINT_1_X,
                panelY + BUTTON_Y,
                BUTTON_W,
                BUTTON_H
        );

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                RECALL_WAYPOINT_2,
                panelX + WAYPOINT_2_X,
                panelY + BUTTON_Y,
                BUTTON_W,
                BUTTON_H
        );

        drawTooltip(graphics, mouseX, mouseY, panelX, panelY);
    }

    public static boolean mouseClicked(
            int upgradeSlotIndex,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY,
            boolean shiftDown,
            UpgradeConfigPanel.ActionSender sender
    ) {
        int buttonY = panelY + BUTTON_Y;

        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + HOME_X,
                buttonY,
                BUTTON_W,
                BUTTON_H
        )) {
            sender.send(
                    upgradeSlotIndex,
                    shiftDown
                            ? BackpackUpgradeConfigAction.SET_RECALL_HOME
                            : BackpackUpgradeConfigAction.TELEPORT_RECALL_HOME
            );
            return true;
        }

        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + WAYPOINT_1_X,
                buttonY,
                BUTTON_W,
                BUTTON_H
        )) {
            sender.send(
                    upgradeSlotIndex,
                    shiftDown
                            ? BackpackUpgradeConfigAction.SET_RECALL_WAYPOINT_1
                            : BackpackUpgradeConfigAction.TELEPORT_RECALL_WAYPOINT_1
            );
            return true;
        }

        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + WAYPOINT_2_X,
                buttonY,
                BUTTON_W,
                BUTTON_H
        )) {
            sender.send(
                    upgradeSlotIndex,
                    shiftDown
                            ? BackpackUpgradeConfigAction.SET_RECALL_WAYPOINT_2
                            : BackpackUpgradeConfigAction.TELEPORT_RECALL_WAYPOINT_2
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
            int panelY
    ) {
        int buttonY = panelY + BUTTON_Y;

        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + HOME_X,
                buttonY,
                BUTTON_W,
                BUTTON_H
        )) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Home: Click to teleport. Shift-click to bind."),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + WAYPOINT_1_X,
                buttonY,
                BUTTON_W,
                BUTTON_H
        )) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Waypoint I: Click to teleport. Shift-click to bind."),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (UpgradePanelUtil.isInside(
                mouseX,
                mouseY,
                panelX + WAYPOINT_2_X,
                buttonY,
                BUTTON_W,
                BUTTON_H
        )) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Waypoint II: Click to teleport. Shift-click to bind."),
                    mouseX,
                    mouseY
            );
        }
    }
}