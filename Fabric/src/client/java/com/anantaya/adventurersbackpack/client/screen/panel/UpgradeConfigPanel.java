package com.anantaya.adventurersbackpack.client.screen.panel;

import com.anantaya.adventurersbackpack.network.CartographersCasePayload;
import com.anantaya.adventurersbackpack.upgrade.autopickup.AutoPickupMode;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseData;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseHelper;
import com.anantaya.adventurersbackpack.upgrade.config.RestockConfig;
import com.anantaya.adventurersbackpack.upgrade.foodpouch.FoodPouchMode;
import com.anantaya.adventurersbackpack.client.gui.BackpackGuiRenderer;

import com.anantaya.adventurersbackpack.upgrade.config.AutoPickupConfig;
import com.anantaya.adventurersbackpack.upgrade.config.FoodPouchConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
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

    private static final int RECALL_PANEL_W = 44;
    private static final int RECALL_PANEL_H = 18;

    private static final int RECALL_HOME_X = 5;
    private static final int RECALL_WAYPOINT_1_X = 17;
    private static final int RECALL_WAYPOINT_2_X = 29;
    private static final int RECALL_BUTTON_Y = 4;
    private static final int RECALL_BUTTON_W = 10;
    private static final int RECALL_BUTTON_H = 10;

    private static final int CARTOGRAPHER_PANEL_W = 64;
    private static final int CARTOGRAPHER_PANEL_H = 64;

    private static final int CARTOGRAPHER_SLOT_SIZE = 18;
    private static final int CARTOGRAPHER_GRID_X = 5;
    private static final int CARTOGRAPHER_GRID_Y = 5;

    private static final int CARTOGRAPHER_DEATH_SLOT = 0;

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

    private static final Identifier RECALL_HOME =
            texture("config/recall_home");

    private static final Identifier RECALL_WAYPOINT_1 =
            texture("config/recall_waypoint_1");

    private static final Identifier RECALL_WAYPOINT_2 =
            texture("config/recall_waypoint_2");

    private UpgradeConfigPanel() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            Slot slot,
            ItemStack upgradeStack,
            ItemStack backpackStack,
            HolderLookup.Provider registries,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        if (!(upgradeStack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
            return;
        }

        if (upgradeItem.getType() == BackpackUpgradeItem.Type.RECALL_RUNE) {
            BackpackGuiRenderer.drawConfigPanel(
                    graphics,
                    panelX,
                    panelY,
                    RECALL_PANEL_W,
                    RECALL_PANEL_H
            );

            drawRecallRunePanel(
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
                    CARTOGRAPHER_PANEL_W,
                    CARTOGRAPHER_PANEL_H
            );

            drawCartographersCasePanel(
                    graphics,
                    backpackStack,
                    registries,
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
        } else if (upgradeItem.getType() == BackpackUpgradeItem.Type.RESTOCK) {
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

    private static void drawCartographersCasePanel(
            GuiGraphicsExtractor graphics,
            ItemStack backpackStack,
            HolderLookup.Provider registries,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int activeSlot = backpackStack.isEmpty()
                ? CartographersCaseHelper.DEATH_SIGNAL_SLOT
                : CartographersCaseData.getActiveSlot(backpackStack);

        for (int i = 0; i < 9; i++) {
            int slotX = cartographerCellX(panelX, i);
            int slotY = cartographerCellY(panelY, i);

            BackpackGuiRenderer.drawSingleSlotFrame(
                    graphics,
                    slotX,
                    slotY
            );

            if (i == activeSlot) {
                drawActiveCartographerCell(graphics, slotX, slotY);
            }

            if (i == CARTOGRAPHER_DEATH_SLOT) {
                drawDeathSignalIcon(graphics, slotX, slotY);
                continue;
            }

            boolean hasCompass = false;

            if (!backpackStack.isEmpty() && registries != null) {
                ItemStack compass = CartographersCaseData.getCompass(
                        backpackStack,
                        CartographersCaseHelper.toCompassInventoryIndex(i),
                        registries
                );

                hasCompass = !compass.isEmpty();
            }

            if (hasCompass) {
                drawFilledCompassMarker(graphics, slotX, slotY, i);
            } else {
                drawEmptyCompassMarker(graphics, slotX, slotY, i);
            }
        }

        drawCartographersCaseTooltip(
                graphics,
                backpackStack,
                registries,
                panelX,
                panelY,
                mouseX,
                mouseY
        );
    }

    private static void drawActiveCartographerCell(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        graphics.fill(x + 1, y + 1, x + 17, y + 2, 0xFFFFD75A);
        graphics.fill(x + 1, y + 16, x + 17, y + 17, 0xFFFFD75A);
        graphics.fill(x + 1, y + 1, x + 2, y + 17, 0xFFFFD75A);
        graphics.fill(x + 16, y + 1, x + 17, y + 17, 0xFFFFD75A);
    }

    private static void drawFilledCompassMarker(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int number
    ) {
        graphics.fill(x + 5, y + 5, x + 13, y + 13, 0xFFB88C4A);
        graphics.fill(x + 7, y + 3, x + 11, y + 15, 0xFFE6E6E6);
        graphics.fill(x + 3, y + 7, x + 15, y + 11, 0xFFE6E6E6);
        graphics.text(
                net.minecraft.client.Minecraft.getInstance().font,
                String.valueOf(number),
                x + 7,
                y + 5,
                0xFF202020,
                true
        );
    }

    private static void drawEmptyCompassMarker(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int number
    ) {
        graphics.text(
                net.minecraft.client.Minecraft.getInstance().font,
                String.valueOf(number),
                x + 7,
                y + 5,
                0xFF777777,
                true
        );
    }

    private static void drawDeathSignalIcon(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        int dark = 0xFF101010;
        int light = 0xFFE6E6E6;
        int mid = 0xFF9A9A9A;

        // tiny skull-ish marker
        graphics.fill(x + 6, y + 4, x + 12, y + 5, light);
        graphics.fill(x + 5, y + 5, x + 13, y + 10, light);
        graphics.fill(x + 6, y + 10, x + 12, y + 13, mid);

        graphics.fill(x + 7, y + 7, x + 8, y + 8, dark);
        graphics.fill(x + 10, y + 7, x + 11, y + 8, dark);
        graphics.fill(x + 8, y + 10, x + 10, y + 11, dark);

        graphics.fill(x + 7, y + 13, x + 8, y + 15, light);
        graphics.fill(x + 10, y + 13, x + 11, y + 15, light);
    }

    private static void drawSmallSlotNumber(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int number
    ) {
        graphics.text(
                net.minecraft.client.Minecraft.getInstance().font,
                String.valueOf(number),
                x + 7,
                y + 5,
                0xFFE6E6E6,
                true
        );
    }

    private static int cartographerCellX(int panelX, int index) {
        int col = index % 3;
        return panelX + CARTOGRAPHER_GRID_X + col * CARTOGRAPHER_SLOT_SIZE;
    }

    private static int cartographerCellY(int panelY, int index) {
        int row = index / 3;
        return panelY + CARTOGRAPHER_GRID_Y + row * CARTOGRAPHER_SLOT_SIZE;
    }

    private static void drawCartographersCaseTooltip(
            GuiGraphicsExtractor graphics,
            ItemStack backpackStack,
            HolderLookup.Provider registries,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        for (int i = 0; i < 9; i++) {
            int slotX = cartographerCellX(panelX, i);
            int slotY = cartographerCellY(panelY, i);

            if (!isInside(
                    mouseX,
                    mouseY,
                    slotX,
                    slotY,
                    CARTOGRAPHER_SLOT_SIZE,
                    CARTOGRAPHER_SLOT_SIZE
            )) {
                continue;
            }

            if (i == CARTOGRAPHER_DEATH_SLOT) {
                boolean hasDeath = !backpackStack.isEmpty()
                        && CartographersCaseData.getDeathTarget(backpackStack).isPresent();

                graphics.setTooltipForNextFrame(
                        Component.literal(hasDeath
                                ? "Death Signal: click to track your last death."
                                : "Death Signal: no death recorded yet."),
                        mouseX,
                        mouseY
                );
                return;
            }

            if (backpackStack.isEmpty() || registries == null) {
                graphics.setTooltipForNextFrame(
                        Component.literal("Lodestone Compass Slot " + i),
                        mouseX,
                        mouseY
                );
                return;
            }

            ItemStack compass = CartographersCaseData.getCompass(
                    backpackStack,
                    CartographersCaseHelper.toCompassInventoryIndex(i),
                    registries
            );

            if (compass.isEmpty()) {
                graphics.setTooltipForNextFrame(
                        Component.literal("Empty lodestone compass slot " + i),
                        mouseX,
                        mouseY
                );
                return;
            }

            graphics.setTooltipForNextFrame(
                    Component.literal("Tracking: " + compass.getHoverName().getString()),
                    mouseX,
                    mouseY
            );
            return;
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

        if (upgradeItem.getType() == BackpackUpgradeItem.Type.RECALL_RUNE) {
            return mouseClickedRecallRune(
                    upgradeSlotIndex,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY,
                    shiftDown,
                    sender
            );
        }

        if (upgradeItem.getType() == BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE) {
            return mouseClickedCartographersCase(
                    upgradeSlotIndex,
                    panelX,
                    panelY,
                    mouseX,
                    mouseY,
                    sender
            );
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

    private static boolean mouseClickedCartographersCase(
            int upgradeSlotIndex,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY,
            ActionSender sender
    ) {
        for (int i = 0; i < 9; i++) {
            int slotX = cartographerCellX(panelX, i);
            int slotY = cartographerCellY(panelY, i);

            if (!isInside(
                    mouseX,
                    mouseY,
                    slotX,
                    slotY,
                    CARTOGRAPHER_SLOT_SIZE,
                    CARTOGRAPHER_SLOT_SIZE
            )) {
                continue;
            }

            ClientPlayNetworking.send(
                    new CartographersCasePayload(
                            upgradeSlotIndex,
                            i
                    )
            );

            return true;
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

    private static void drawRecallRunePanel(
            GuiGraphicsExtractor graphics,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        BackpackGuiRenderer.drawTextureButton(
                graphics,
                RECALL_HOME,
                panelX + RECALL_HOME_X,
                panelY + RECALL_BUTTON_Y,
                RECALL_BUTTON_W,
                RECALL_BUTTON_H
        );

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                RECALL_WAYPOINT_1,
                panelX + RECALL_WAYPOINT_1_X,
                panelY + RECALL_BUTTON_Y,
                RECALL_BUTTON_W,
                RECALL_BUTTON_H
        );

        BackpackGuiRenderer.drawTextureButton(
                graphics,
                RECALL_WAYPOINT_2,
                panelX + RECALL_WAYPOINT_2_X,
                panelY + RECALL_BUTTON_Y,
                RECALL_BUTTON_W,
                RECALL_BUTTON_H
        );

        drawRecallRuneTooltip(
                graphics,
                mouseX,
                mouseY,
                panelX,
                panelY
        );
    }

    private static boolean mouseClickedRecallRune(
            int upgradeSlotIndex,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY,
            boolean shiftDown,
            ActionSender sender
    ) {
        int buttonY = panelY + RECALL_BUTTON_Y;

        if (isInside(
                mouseX,
                mouseY,
                panelX + RECALL_HOME_X,
                buttonY,
                RECALL_BUTTON_W,
                RECALL_BUTTON_H
        )) {
            sender.send(
                    upgradeSlotIndex,
                    shiftDown
                            ? BackpackUpgradeConfigAction.SET_RECALL_HOME
                            : BackpackUpgradeConfigAction.TELEPORT_RECALL_HOME
            );
            return true;
        }

        if (isInside(
                mouseX,
                mouseY,
                panelX + RECALL_WAYPOINT_1_X,
                buttonY,
                RECALL_BUTTON_W,
                RECALL_BUTTON_H
        )) {
            sender.send(
                    upgradeSlotIndex,
                    shiftDown
                            ? BackpackUpgradeConfigAction.SET_RECALL_WAYPOINT_1
                            : BackpackUpgradeConfigAction.TELEPORT_RECALL_WAYPOINT_1
            );
            return true;
        }

        if (isInside(
                mouseX,
                mouseY,
                panelX + RECALL_WAYPOINT_2_X,
                buttonY,
                RECALL_BUTTON_W,
                RECALL_BUTTON_H
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

    private static void drawRecallRuneTooltip(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY
    ) {
        int buttonY = panelY + RECALL_BUTTON_Y;

        if (isInside(
                mouseX,
                mouseY,
                panelX + RECALL_HOME_X,
                buttonY,
                RECALL_BUTTON_W,
                RECALL_BUTTON_H
        )) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Home: Click to teleport. Shift-click to bind."),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (isInside(
                mouseX,
                mouseY,
                panelX + RECALL_WAYPOINT_1_X,
                buttonY,
                RECALL_BUTTON_W,
                RECALL_BUTTON_H
        )) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Waypoint I: Click to teleport. Shift-click to bind."),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (isInside(
                mouseX,
                mouseY,
                panelX + RECALL_WAYPOINT_2_X,
                buttonY,
                RECALL_BUTTON_W,
                RECALL_BUTTON_H
        )) {
            graphics.setTooltipForNextFrame(
                    Component.literal("Waypoint II: Click to teleport. Shift-click to bind."),
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