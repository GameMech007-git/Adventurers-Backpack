package com.anantaya.adventurersbackpack.client.screen.panel;

import com.anantaya.adventurersbackpack.upgrade.autopickup.AutoPickupMode;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.config.RestockConfig;
import com.anantaya.adventurersbackpack.upgrade.foodpouch.FoodPouchMode;
import com.anantaya.adventurersbackpack.client.gui.BackpackGuiRenderer;

import com.anantaya.adventurersbackpack.upgrade.config.AutoPickupConfig;
import com.anantaya.adventurersbackpack.upgrade.config.FoodPouchConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class UpgradeConfigPanel {

    private static final String MOD_ID = "adventurersbackpack";

    private static final int PANEL_W = 32;
    private static final int PANEL_H = 18;

    private static final int MODE_BUTTON_X = 5;
    private static final int MODE_BUTTON_Y = 4;
    private static final int MODE_BUTTON_W = 10;
    private static final int MODE_BUTTON_H = 10;

    private static final int SECOND_BUTTON_X = 17;
    private static final int SECOND_BUTTON_Y = 4;
    private static final int SECOND_BUTTON_W = 10;
    private static final int SECOND_BUTTON_H = 10;

    private static final Identifier AUTO_MODE_OFF =
            texture("config/auto_mode_off");

    private static final Identifier AUTO_MODE_MATCHING =
            texture("config/auto_mode_matching");

    private static final Identifier AUTO_MODE_ALL =
            texture("config/auto_mode_all");

    private static final Identifier IGNORE_DROPS_ON =
            texture("config/ignore_drops_on");

    private static final Identifier IGNORE_DROPS_OFF =
            texture("config/ignore_drops_off");

    private static final Identifier FOOD_MODE_OFF =
            texture("config/food_mode_off");

    private static final Identifier FOOD_MODE_SMART =
            texture("config/food_mode_smart");

    private static final Identifier FOOD_MODE_LOW_VALUE =
            texture("config/food_mode_low_value");

    private static final Identifier FOOD_MODE_EMERGENCY =
            texture("config/food_mode_emergency");

    private static final Identifier FOOD_WARNING_ON =
            texture("config/food_warning_on");

    private static final Identifier FOOD_WARNING_OFF =
            texture("config/food_warning_off");

    private static final Identifier RESTOCK_BUCKETS_ON =
            texture("config/restock_buckets_on");

    private static final Identifier RESTOCK_BUCKETS_OFF =
            texture("config/restock_buckets_off");

    private static final Identifier RESTOCK_TOOLS_ON =
            texture("config/restock_tools_on");

    private static final Identifier RESTOCK_TOOLS_OFF =
            texture("config/restock_tools_off");

    private UpgradeConfigPanel() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            Slot slot,
            ItemStack upgradeStack,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        if (!(upgradeStack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return;
        }

        BackpackGuiRenderer.drawConfigPanel(
                graphics,
                panelX,
                panelY,
                PANEL_W,
                PANEL_H
        );

        if (upgradeItem.getType() == BackpackUpgradeItem.Type.AUTO_PICKUP) {
            drawAutoPickupPanel(
                    graphics,
                    upgradeStack,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY
            );
        } else if (upgradeItem.getType() == BackpackUpgradeItem.Type.FOOD_POUCH) {
            drawFoodPouchPanel(
                    graphics,
                    upgradeStack,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY
            );
        }
        else if (upgradeItem.getType() == BackpackUpgradeItem.Type.RESTOCK) {
            drawRestockPanel(
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
            ActionSender sender
    ) {
        if (!(upgradeStack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return false;
        }

        int modeX = panelX + MODE_BUTTON_X;
        int modeY = panelY + MODE_BUTTON_Y;

        if (isInside(mouseX, mouseY, modeX, modeY, MODE_BUTTON_W, MODE_BUTTON_H)) {
            if (upgradeItem.getType() == BackpackUpgradeItem.Type.AUTO_PICKUP) {
                sender.send(
                        upgradeSlotIndex,
                        BackpackUpgradeConfigAction.CYCLE_AUTO_PICKUP_MODE
                );
                return true;
            }

            if (upgradeItem.getType() == BackpackUpgradeItem.Type.FOOD_POUCH) {
                sender.send(
                        upgradeSlotIndex,
                        BackpackUpgradeConfigAction.CYCLE_FOOD_POUCH_MODE
                );
                return true;
            }

            if (upgradeItem.getType() == BackpackUpgradeItem.Type.RESTOCK) {
                sender.send(
                        upgradeSlotIndex,
                        BackpackUpgradeConfigAction.TOGGLE_RESTOCK_TOOLS
                );
                return true;
            }
        }

        int secondX = panelX + SECOND_BUTTON_X;
        int secondY = panelY + SECOND_BUTTON_Y;

        if (isInside(mouseX, mouseY, secondX, secondY, SECOND_BUTTON_W, SECOND_BUTTON_H)) {
            if (upgradeItem.getType() == BackpackUpgradeItem.Type.AUTO_PICKUP) {
                sender.send(
                        upgradeSlotIndex,
                        BackpackUpgradeConfigAction.TOGGLE_IGNORE_PLAYER_DROPS
                );
                return true;
            }

            if (upgradeItem.getType() == BackpackUpgradeItem.Type.FOOD_POUCH) {
                sender.send(
                        upgradeSlotIndex,
                        BackpackUpgradeConfigAction.TOGGLE_FOOD_POUCH_EMPTY_WARNING
                );
                return true;
            }

            if (upgradeItem.getType() == BackpackUpgradeItem.Type.RESTOCK) {
                sender.send(
                        upgradeSlotIndex,
                        BackpackUpgradeConfigAction.TOGGLE_RESTOCK_BUCKETS
                );
                return true;
            }
        }

        return false;
    }

    private static void drawAutoPickupPanel(
            GuiGraphicsExtractor graphics,
            ItemStack upgradeStack,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        AutoPickupMode mode =
                AutoPickupConfig.getMode(upgradeStack);

        boolean ignoreDrops =
                AutoPickupConfig.shouldIgnorePlayerDrops(upgradeStack);

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

        drawAutoPickupTooltip(
                graphics,
                mouseX,
                mouseY,
                panelX,
                panelY,
                mode,
                ignoreDrops
        );
    }

    private static void drawFoodPouchPanel(
            GuiGraphicsExtractor graphics,
            ItemStack upgradeStack,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        FoodPouchMode mode =
                FoodPouchConfig.getMode(upgradeStack);

        boolean warning =
                FoodPouchConfig.shouldWarnWhenEmpty(upgradeStack);

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

        drawFoodPouchTooltip(
                graphics,
                mouseX,
                mouseY,
                panelX,
                panelY,
                mode,
                warning
        );
    }

    private static void drawAutoPickupTooltip(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY,
            AutoPickupMode mode,
            boolean ignoreDrops
    ) {
        int modeX = panelX + MODE_BUTTON_X;
        int modeY = panelY + MODE_BUTTON_Y;

        if (isInside(mouseX, mouseY, modeX, modeY, MODE_BUTTON_W, MODE_BUTTON_H)) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Mode: " + mode.displayName()),
                    mouseX,
                    mouseY
            );
            return;
        }

        int secondX = panelX + SECOND_BUTTON_X;
        int secondY = panelY + SECOND_BUTTON_Y;

        if (isInside(mouseX, mouseY, secondX, secondY, SECOND_BUTTON_W, SECOND_BUTTON_H)) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Ignore player drops: " + (ignoreDrops ? "On" : "Off")),
                    mouseX,
                    mouseY
            );
        }
    }

    private static void drawFoodPouchTooltip(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY,
            FoodPouchMode mode,
            boolean warning
    ) {
        int modeX = panelX + MODE_BUTTON_X;
        int modeY = panelY + MODE_BUTTON_Y;

        if (isInside(mouseX, mouseY, modeX, modeY, MODE_BUTTON_W, MODE_BUTTON_H)) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Food mode: " + mode.displayName()),
                    mouseX,
                    mouseY
            );
            return;
        }

        int secondX = panelX + SECOND_BUTTON_X;
        int secondY = panelY + SECOND_BUTTON_Y;

        if (isInside(mouseX, mouseY, secondX, secondY, SECOND_BUTTON_W, SECOND_BUTTON_H)) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Empty warning: " + (warning ? "On" : "Off")),
                    mouseX,
                    mouseY
            );
        }
    }

    private static void drawRestockPanel(
            GuiGraphicsExtractor graphics,
            ItemStack upgradeStack,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        boolean tools =
                RestockConfig.shouldRestockTools(upgradeStack);

        boolean buckets =
                RestockConfig.shouldRestockBuckets(upgradeStack);

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

        drawRestockTooltip(
                graphics,
                mouseX,
                mouseY,
                panelX,
                panelY,
                tools,
                buckets
        );
    }

    private static void drawRestockTooltip(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY,
            boolean tools,
            boolean buckets
    ) {
        int modeX = panelX + MODE_BUTTON_X;
        int modeY = panelY + MODE_BUTTON_Y;

        if (isInside(mouseX, mouseY, modeX, modeY, MODE_BUTTON_W, MODE_BUTTON_H)) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Restock tools: " + (tools ? "On" : "Off")),
                    mouseX,
                    mouseY
            );
            return;
        }

        int secondX = panelX + SECOND_BUTTON_X;
        int secondY = panelY + SECOND_BUTTON_Y;

        if (isInside(mouseX, mouseY, secondX, secondY, SECOND_BUTTON_W, SECOND_BUTTON_H)) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Restock buckets: " + (buckets ? "On" : "Off")),
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

    private static Identifier getFoodPouchModeTexture(FoodPouchMode mode) {
        return switch (mode) {
            case OFF -> FOOD_MODE_OFF;
            case SMART -> FOOD_MODE_SMART;
            case LOW_VALUE -> FOOD_MODE_LOW_VALUE;
            case EMERGENCY -> FOOD_MODE_EMERGENCY;
        };
    }

    private static Identifier texture(String path) {
        return Identifier.fromNamespaceAndPath(
                MOD_ID,
                "textures/gui/" + path + ".png"
        );
    }

    private static boolean isInside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int w,
            int h
    ) {
        return mouseX >= x
                && mouseX < x + w
                && mouseY >= y
                && mouseY < y + h;
    }

    @FunctionalInterface
    public interface ActionSender {
        void send(int upgradeSlotIndex, BackpackUpgradeConfigAction action);
    }
}