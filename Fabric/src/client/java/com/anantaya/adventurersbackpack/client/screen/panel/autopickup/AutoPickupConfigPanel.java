package com.anantaya.adventurersbackpack.client.screen.panel.autopickup;

import com.anantaya.adventurersbackpack.client.gui.BackpackGuiRenderer;
import com.anantaya.adventurersbackpack.client.screen.panel.UpgradeConfigPanel;
import com.anantaya.adventurersbackpack.client.screen.panel.UpgradePanelUtil;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import com.anantaya.adventurersbackpack.upgrade.autopickup.AutoPickupMode;
import com.anantaya.adventurersbackpack.upgrade.config.AutoPickupConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class AutoPickupConfigPanel {

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

    private static final Identifier AUTO_MODE_OFF =
            UpgradePanelUtil.texture("config/auto_mode_off");

    private static final Identifier AUTO_MODE_MATCHING =
            UpgradePanelUtil.texture("config/auto_mode_matching");

    private static final Identifier AUTO_MODE_ALL =
            UpgradePanelUtil.texture("config/auto_mode_all");

    private static final Identifier IGNORE_DROPS_ON =
            UpgradePanelUtil.texture("config/ignore_drops_on");

    private static final Identifier IGNORE_DROPS_OFF =
            UpgradePanelUtil.texture("config/ignore_drops_off");

    private AutoPickupConfigPanel() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            ItemStack upgradeStack,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        AutoPickupMode mode = AutoPickupConfig.getMode(upgradeStack);
        boolean ignoreDrops = AutoPickupConfig.shouldIgnorePlayerDrops(upgradeStack);

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                getAutoPickupModeTexture(mode),
                panelX + MODE_BUTTON_X,
                panelY + MODE_BUTTON_Y,
                MODE_BUTTON_W,
                MODE_BUTTON_H
        );

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                ignoreDrops ? IGNORE_DROPS_ON : IGNORE_DROPS_OFF,
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
                mode,
                ignoreDrops
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
                    BackpackUpgradeConfigAction.CYCLE_AUTO_PICKUP_MODE
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
                    BackpackUpgradeConfigAction.TOGGLE_IGNORE_PLAYER_DROPS
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
            AutoPickupMode mode,
            boolean ignoreDrops
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
                    Component.literal("Mode: " + mode.displayName()),
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
                    Component.literal("Ignore player drops: " + (ignoreDrops ? "On" : "Off")),
                    mouseX,
                    mouseY
            );
        }
    }

    private static Identifier getAutoPickupModeTexture(AutoPickupMode mode) {
        return switch (mode) {
            case OFF -> AUTO_MODE_OFF;
            case MATCHING_ONLY -> AUTO_MODE_MATCHING;
            case PICKUP_ALL -> AUTO_MODE_ALL;
        };
    }
}