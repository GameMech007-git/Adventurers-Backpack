package com.anantaya.adventurersbackpack.client.screen.panel.foodpouch;

import com.anantaya.adventurersbackpack.client.gui.BackpackGuiRenderer;
import com.anantaya.adventurersbackpack.client.screen.panel.UpgradeConfigPanel;
import com.anantaya.adventurersbackpack.client.screen.panel.UpgradePanelUtil;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import com.anantaya.adventurersbackpack.upgrade.config.FoodPouchConfig;
import com.anantaya.adventurersbackpack.upgrade.foodpouch.FoodPouchMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class FoodPouchConfigPanel {

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

    private static final Identifier FOOD_MODE_OFF =
            UpgradePanelUtil.texture("config/food_mode_off");

    private static final Identifier FOOD_MODE_SMART =
            UpgradePanelUtil.texture("config/food_mode_smart");

    private static final Identifier FOOD_MODE_LOW_VALUE =
            UpgradePanelUtil.texture("config/food_mode_low_value");

    private static final Identifier FOOD_MODE_EMERGENCY =
            UpgradePanelUtil.texture("config/food_mode_emergency");

    private static final Identifier FOOD_WARNING_ON =
            UpgradePanelUtil.texture("config/food_warning_on");

    private static final Identifier FOOD_WARNING_OFF =
            UpgradePanelUtil.texture("config/food_warning_off");

    private FoodPouchConfigPanel() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            ItemStack upgradeStack,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        FoodPouchMode mode = FoodPouchConfig.getMode(upgradeStack);
        boolean warning = FoodPouchConfig.shouldWarnWhenEmpty(upgradeStack);

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                getFoodPouchModeTexture(mode),
                panelX + MODE_BUTTON_X,
                panelY + MODE_BUTTON_Y,
                MODE_BUTTON_W,
                MODE_BUTTON_H
        );

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                warning ? FOOD_WARNING_ON : FOOD_WARNING_OFF,
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
                warning
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
                    BackpackUpgradeConfigAction.CYCLE_FOOD_POUCH_MODE
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
                    BackpackUpgradeConfigAction.TOGGLE_FOOD_POUCH_EMPTY_WARNING
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
            FoodPouchMode mode,
            boolean warning
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
                    Component.literal("Food mode: " + mode.displayName()),
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
                    Component.literal("Empty warning: " + (warning ? "On" : "Off")),
                    mouseX,
                    mouseY
            );
        }
    }

    private static Identifier getFoodPouchModeTexture(FoodPouchMode mode) {
        return switch (mode) {
            case OFF -> FOOD_MODE_OFF;
            case SMART -> FOOD_MODE_SMART;
            case LOW_VALUE -> FOOD_MODE_LOW_VALUE;
            case EMERGENCY -> FOOD_MODE_EMERGENCY;
        };
    }
}