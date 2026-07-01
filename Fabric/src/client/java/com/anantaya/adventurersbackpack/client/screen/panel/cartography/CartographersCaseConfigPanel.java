package com.anantaya.adventurersbackpack.client.screen.panel.cartography;

import com.anantaya.adventurersbackpack.client.gui.BackpackGuiRenderer;
import com.anantaya.adventurersbackpack.client.screen.panel.UpgradePanelUtil;
import com.anantaya.adventurersbackpack.network.CartographersCasePayload;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseData;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CartographersCaseConfigPanel {

    public static final int WIDTH = 64;
    public static final int HEIGHT = 74;

    private static final int SLOT_SIZE = 18;
    private static final int GRID_X = 5;
    private static final int GRID_Y = 15;

    private static final int DEATH_SLOT = 0;

    private CartographersCaseConfigPanel() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            ItemStack backpackStack,
            HolderLookup.Provider registries,
            int syncedActiveSlot,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int activeSlot = syncedActiveSlot;

        drawStatusText(
                graphics,
                panelX,
                panelY,
                activeSlot
        );

        for (int i = 0; i < 9; i++) {
            int slotX = cellX(panelX, i);
            int slotY = cellY(panelY, i);

            BackpackGuiRenderer.drawSingleSlotFrame(
                    graphics,
                    slotX,
                    slotY
            );

            if (i == activeSlot) {
                drawActiveCell(graphics, slotX, slotY);
            }

            if (i == DEATH_SLOT) {
                drawDeathSignalIcon(graphics, slotX, slotY);
                continue;
            }

            drawEmptyCompassMarker(graphics, slotX, slotY, i);
        }

        drawTooltip(
                graphics,
                backpackStack,
                panelX,
                panelY,
                mouseX,
                mouseY
        );
    }

    public static boolean mouseClicked(
            int upgradeSlotIndex,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY,
            boolean shiftDown
    ) {
        for (int i = 0; i < 9; i++) {
            int slotX = cellX(panelX, i);
            int slotY = cellY(panelY, i);

            if (!UpgradePanelUtil.isInside(
                    mouseX,
                    mouseY,
                    slotX,
                    slotY,
                    SLOT_SIZE,
                    SLOT_SIZE
            )) {
                continue;
            }

            if (i == DEATH_SLOT) {
                ClientPlayNetworking.send(
                        new CartographersCasePayload(
                                upgradeSlotIndex,
                                i
                        )
                );

                return true;
            }

            if (!shiftDown) {
                return false;
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

    private static void drawStatusText(
            GuiGraphicsExtractor graphics,
            int panelX,
            int panelY,
            int activeSlot
    ) {
        String text;

        if (activeSlot == CartographersCaseHelper.NO_ACTIVE_SLOT) {
            text = "OFF";
        } else if (activeSlot == DEATH_SLOT) {
            text = "DEATH";
        } else {
            text = "SLOT " + activeSlot;
        }

        int color = activeSlot == CartographersCaseHelper.NO_ACTIVE_SLOT
                ? 0xFFAA5555
                : 0xFFFFD75A;

        graphics.text(
                net.minecraft.client.Minecraft.getInstance().font,
                text,
                panelX + 6,
                panelY + 4,
                color,
                true
        );
    }

    private static void drawActiveCell(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        graphics.fill(x + 1, y + 1, x + 17, y + 2, 0xFFFFD75A);
        graphics.fill(x + 1, y + 16, x + 17, y + 17, 0xFFFFD75A);
        graphics.fill(x + 1, y + 1, x + 2, y + 17, 0xFFFFD75A);
        graphics.fill(x + 16, y + 1, x + 17, y + 17, 0xFFFFD75A);
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

        graphics.fill(x + 6, y + 4, x + 12, y + 5, light);
        graphics.fill(x + 5, y + 5, x + 13, y + 10, light);
        graphics.fill(x + 6, y + 10, x + 12, y + 13, mid);

        graphics.fill(x + 7, y + 7, x + 8, y + 8, dark);
        graphics.fill(x + 10, y + 7, x + 11, y + 8, dark);
        graphics.fill(x + 8, y + 10, x + 10, y + 11, dark);

        graphics.fill(x + 7, y + 13, x + 8, y + 15, light);
        graphics.fill(x + 10, y + 13, x + 11, y + 15, light);
    }

    private static void drawTooltip(
            GuiGraphicsExtractor graphics,
            ItemStack backpackStack,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        for (int i = 0; i < 9; i++) {
            int slotX = cellX(panelX, i);
            int slotY = cellY(panelY, i);

            if (!UpgradePanelUtil.isInside(
                    mouseX,
                    mouseY,
                    slotX,
                    slotY,
                    SLOT_SIZE,
                    SLOT_SIZE
            )) {
                continue;
            }

            if (i == DEATH_SLOT) {
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

            graphics.setComponentTooltipForNextFrame(
                    net.minecraft.client.Minecraft.getInstance().font,
                    java.util.List.of(
                            Component.literal("Lodestone Compass"),
                            Component.literal("Shift-click to activate")
                    ),
                    mouseX,
                    mouseY
            );

            return;
        }
    }

    private static int cellX(int panelX, int index) {
        int col = index % 3;
        return panelX + GRID_X + col * SLOT_SIZE;
    }

    private static int cellY(int panelY, int index) {
        int row = index / 3;
        return panelY + GRID_Y + row * SLOT_SIZE;
    }
}