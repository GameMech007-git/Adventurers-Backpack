package com.anantaya.adventurersbackpack.client.gui;

import com.anantaya.adventurersbackpack.backpack.BackpackMenuLayout;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;

import com.anantaya.adventurersbackpack.upgrade.fluidstorage.FluidStorageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class BackpackGuiRenderer {

    private BackpackGuiRenderer() {
    }

    public static void drawBase(
            GuiGraphicsExtractor graphics,
            BackpackMenuLayout layout,
            int left,
            int top
    ) {
        drawMainPanel(graphics, layout, left, top);
        drawSectionBoxes(graphics, layout, left, top);
        drawUpgradeStrip(graphics, layout, left, top);
        drawTopUtilityBox(graphics, left, top);
    }

    /*public static void drawSlotFrames(
            GuiGraphicsExtractor graphics,
            List<Slot> slots,
            int left,
            int top
    ) {
        for (Slot slot : slots) {
            drawSlotFrame(graphics, left + slot.x - 1, top + slot.y - 1);
        }
    }*/

    public static void drawLockedUpgradeSlots(
            GuiGraphicsExtractor graphics,
            BackpackMenuLayout layout,
            int left,
            int top
    ) {
        for (int i = layout.tier.upgradeSlots; i < BackpackTier.MAX_UPGRADE_SLOTS; i++) {
            int sx = left + layout.upgradeSlotX() - 1;
            int sy = top + layout.upgradeSlotY(i) - 1;

            graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF1A1A1A);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF4A4A4A);

            drawX(graphics, sx, sy);
        }
    }

    public static void drawButtons(
            GuiGraphicsExtractor graphics,
            List<BackpackGuiButton> buttons,
            int mouseX,
            int mouseY,
            BackpackTier tier
    ) {
        for (BackpackGuiButton button : buttons) {
            drawButton(graphics, button, button.isMouseOver(mouseX, mouseY));
        }
    }

    public static void drawButtonTooltips(
            GuiGraphicsExtractor graphics,
            List<BackpackGuiButton> buttons,
            int mouseX,
            int mouseY
    ) {
        for (BackpackGuiButton button : buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(button.tooltip, mouseX, mouseY);
                return;
            }
        }
    }

    public static void drawConfigPanel(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int w,
            int h
    ) {
        drawSectionBox(graphics, x, y, w, h);
    }

    public static void drawTextureButton(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int x,
            int y,
            int width,
            int height
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                0.0F,
                0.0F,
                width,
                height,
                width,
                height
        );
    }

    private static void drawMainPanel(
            GuiGraphicsExtractor graphics,
            BackpackMenuLayout layout,
            int x,
            int y
    ) {
        int w = BackpackMenuLayout.MAIN_WIDTH;
        int h = layout.guiHeight;

        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF151515);
        graphics.fill(x, y, x + w, y + h, 0xFF2D2D2D);

        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFF5A5A5A);
        graphics.fill(x + 1, y + 1, x + 2, y + h - 1, 0xFF5A5A5A);

        graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFF101010);
        graphics.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0xFF101010);
    }

    private static void drawSectionBoxes(
            GuiGraphicsExtractor graphics,
            BackpackMenuLayout layout,
            int x,
            int y
    ) {
        drawSectionBox(
                graphics,
                x + BackpackMenuLayout.CONTENT_BOX_X,
                y + layout.magicY - 4,
                BackpackMenuLayout.CONTENT_BOX_WIDTH,
                layout.tier.protectedRows() * 18 + 8
        );

        drawSectionBox(
                graphics,
                x + BackpackMenuLayout.CONTENT_BOX_X,
                y + layout.normalY - 4,
                BackpackMenuLayout.CONTENT_BOX_WIDTH,
                layout.tier.normalRows() * 18 + 8
        );

        drawSectionBox(
                graphics,
                x + BackpackMenuLayout.CONTENT_BOX_X,
                y + layout.playerInvY - 4,
                BackpackMenuLayout.CONTENT_BOX_WIDTH,
                3 * 18 + 8
        );

        drawSectionBox(
                graphics,
                x + BackpackMenuLayout.CONTENT_BOX_X,
                y + layout.hotbarY - 4,
                BackpackMenuLayout.CONTENT_BOX_WIDTH,
                18 + 8
        );
    }

    private static void drawTopUtilityBox(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        int boxX = x + 128;
        int boxY = y + 4;
        int boxW = 40;
        int boxH = 14;

        drawSectionBox(graphics, boxX, boxY, boxW, boxH);
    }

    private static void drawUpgradeStrip(
            GuiGraphicsExtractor graphics,
            BackpackMenuLayout layout,
            int x,
            int y
    ) {
        int stripX = x + 181;
        int stripY = y + layout.upgradeSlotY(0) - 4;
        int stripW = 23;
        int stripH = (BackpackTier.MAX_UPGRADE_SLOTS + 1) * 18 + 8;

        drawSectionBox(graphics, stripX, stripY, stripW, stripH);
    }

    private static void drawSectionBox(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int w,
            int h
    ) {
        graphics.fill(x, y, x + w, y + h, 0xFF242424);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF373737);

        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFF707070);
        graphics.fill(x + 1, y + 1, x + 2, y + h - 1, 0xFF707070);

        graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFF171717);
        graphics.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0xFF171717);
    }

    private static void drawSlotFrame(GuiGraphicsExtractor graphics, int sx, int sy) {
        graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF151515);
        graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF6F6F6F);

        graphics.fill(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF2C2C2C);
        graphics.fill(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF2C2C2C);

        graphics.fill(sx + 1, sy + 16, sx + 17, sy + 17, 0xFFD9D9D9);
        graphics.fill(sx + 16, sy + 1, sx + 17, sy + 17, 0xFFD9D9D9);
    }

    private static void drawButton(
            GuiGraphicsExtractor graphics,
            BackpackGuiButton button,
            boolean hovered
    ) {
        int x = button.x;
        int y = button.y;
        int s = button.size;

        int bg = hovered ? 0xFF8A8A8A : 0xFF595959;

        graphics.fill(x, y, x + s, y + s, 0xFF050505);
        graphics.fill(x + 1, y + 1, x + s - 1, y + s - 1, bg);

        graphics.fill(x + 1, y + 1, x + s - 1, y + 2, 0xFFB8B8B8);
        graphics.fill(x + 1, y + 1, x + 2, y + s - 1, 0xFFB8B8B8);

        graphics.fill(x + 1, y + s - 2, x + s - 1, y + s - 1, 0xFF181818);
        graphics.fill(x + s - 2, y + 1, x + s - 1, y + s - 1, 0xFF181818);

        switch (button.type) {
            case SORT -> drawTinySortIcon(graphics, x, y);
            case MOVE_TO_PLAYER -> drawTinyDownIcon(graphics, x, y);
            case MOVE_TO_BACKPACK -> drawTinyUpIcon(graphics, x, y);
            case UPGRADE_CONFIG -> drawTinyGearIcon(graphics, x, y);
        }
    }

    private static void drawX(GuiGraphicsExtractor graphics, int sx, int sy) {
        graphics.fill(sx + 4, sy + 4, sx + 6, sy + 6, 0xFF0F0F0F);
        graphics.fill(sx + 6, sy + 6, sx + 8, sy + 8, 0xFF0F0F0F);
        graphics.fill(sx + 8, sy + 8, sx + 10, sy + 10, 0xFF0F0F0F);
        graphics.fill(sx + 10, sy + 10, sx + 12, sy + 12, 0xFF0F0F0F);
        graphics.fill(sx + 12, sy + 12, sx + 14, sy + 14, 0xFF0F0F0F);

        graphics.fill(sx + 12, sy + 4, sx + 14, sy + 6, 0xFF0F0F0F);
        graphics.fill(sx + 10, sy + 6, sx + 12, sy + 8, 0xFF0F0F0F);
        graphics.fill(sx + 8, sy + 8, sx + 10, sy + 10, 0xFF0F0F0F);
        graphics.fill(sx + 6, sy + 10, sx + 8, sy + 12, 0xFF0F0F0F);
        graphics.fill(sx + 4, sy + 12, sx + 6, sy + 14, 0xFF0F0F0F);
    }

    private static void drawTinySortIcon(GuiGraphicsExtractor graphics, int x, int y) {
        int c = 0xFFE6E6E6;
        int d = 0xFF161616;

        graphics.fill(x + 3, y + 3, x + 8, y + 4, d);
        graphics.fill(x + 3, y + 5, x + 7, y + 6, d);
        graphics.fill(x + 3, y + 7, x + 6, y + 8, d);

        graphics.fill(x + 2, y + 2, x + 7, y + 3, c);
        graphics.fill(x + 2, y + 4, x + 6, y + 5, c);
        graphics.fill(x + 2, y + 6, x + 5, y + 7, c);
    }

    private static void drawTinyDownIcon(GuiGraphicsExtractor graphics, int x, int y) {
        int c = 0xFFE6E6E6;
        int d = 0xFF161616;

        graphics.fill(x + 4, y + 2, x + 6, y + 6, d);
        graphics.fill(x + 3, y + 6, x + 7, y + 7, d);
        graphics.fill(x + 4, y + 7, x + 6, y + 8, d);

        graphics.fill(x + 3, y + 2, x + 5, y + 6, c);
        graphics.fill(x + 2, y + 5, x + 6, y + 6, c);
        graphics.fill(x + 3, y + 6, x + 5, y + 7, c);
    }

    private static void drawTinyUpIcon(GuiGraphicsExtractor graphics, int x, int y) {
        int c = 0xFFE6E6E6;
        int d = 0xFF161616;

        graphics.fill(x + 4, y + 4, x + 6, y + 8, d);
        graphics.fill(x + 3, y + 3, x + 7, y + 4, d);
        graphics.fill(x + 4, y + 2, x + 6, y + 3, d);

        graphics.fill(x + 3, y + 4, x + 5, y + 8, c);
        graphics.fill(x + 2, y + 3, x + 6, y + 4, c);
        graphics.fill(x + 3, y + 2, x + 5, y + 3, c);
    }

    private static void drawTinyGearIcon(GuiGraphicsExtractor graphics, int x, int y) {
        int c = 0xFFE6E6E6;
        int d = 0xFF161616;

        graphics.fill(x + 4, y + 2, x + 7, y + 3, c);
        graphics.fill(x + 3, y + 3, x + 8, y + 4, c);
        graphics.fill(x + 2, y + 4, x + 9, y + 7, c);
        graphics.fill(x + 3, y + 7, x + 8, y + 8, c);
        graphics.fill(x + 4, y + 8, x + 7, y + 9, c);

        graphics.fill(x + 5, y + 5, x + 6, y + 6, d);
    }

    /*private static void drawTinyFilterIcon(GuiGraphicsExtractor graphics, int x, int y) {
        int c = 0xFFE6E6E6;

        graphics.fill(x + 2, y + 2, x + 9, y + 3, c);
        graphics.fill(x + 3, y + 3, x + 8, y + 4, c);
        graphics.fill(x + 4, y + 4, x + 7, y + 5, c);
        graphics.fill(x + 5, y + 5, x + 6, y + 8, c);
    }

    private static void drawTinyDropIcon(GuiGraphicsExtractor graphics, int x, int y) {

        int c = 0xFFE6E6E6;

        graphics.fill(x + 5, y + 2, x + 6, y + 3, c);
        graphics.fill(x + 4, y + 3, x + 7, y + 5, c);
        graphics.fill(x + 3, y + 5, x + 8, y + 8, c);
        graphics.fill(x + 4, y + 8, x + 7, y + 9, c);
    }*/

    public static void drawFluidStoragePanel(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            FluidStorageType type,
            int amount,
            int capacity,
            int panelH
    ) {
        int panelW = 30;

        drawSectionBox(graphics, x, y, panelW, panelH);

        int fakeSlotX = x + 6;
        int fakeSlotY = y + 6;

        drawSlotFrame(graphics, fakeSlotX, fakeSlotY);

        int barX = fakeSlotX + 1;
        int barY = fakeSlotY + 24;
        int barW = 16;
        int segmentH = 6;
        int gap = 1;

        int barH = capacity * segmentH + (capacity - 1) * gap;

        graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF101010);
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF202020);

        int fluidColor = switch (type) {
            case WATER -> 0xFF2F7DFF;
            case LAVA -> 0xFFFF5A1F;
            case MILK -> 0xFFEFEFEF;
            case NONE -> 0x00000000;
        };

        if (type == FluidStorageType.NONE || amount <= 0) {
            return;
        }

        int clampedAmount = Math.max(0, Math.min(capacity, amount));

        for (int i = 0; i < clampedAmount; i++) {
            int segmentY = barY + barH - ((i + 1) * segmentH) - (i * gap);

            graphics.fill(
                    barX + 1,
                    segmentY + 1,
                    barX + barW - 1,
                    segmentY + segmentH - 1,
                    fluidColor
            );
        }

    }

    public static void drawTrashSlot(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        graphics.fill(x - 1, y - 1, x + 18, y + 18, 0xFF151515);
        graphics.fill(x, y, x + 17, y + 17, 0xFF6F6F6F);

        graphics.fill(x, y, x + 17, y + 1, 0xFF2C2C2C);
        graphics.fill(x, y, x + 1, y + 17, 0xFF2C2C2C);

        graphics.fill(x, y + 16, x + 17, y + 17, 0xFFD9D9D9);
        graphics.fill(x + 16, y, x + 17, y + 17, 0xFFD9D9D9);

        // Inner slot background
        graphics.fill(x + 2, y + 2, x + 15, y + 15, 0xFF262626);

        int shadow = 0xFF050505;
        int binDark = 0xFF3A3A3A;
        int binLight = 0xFFCFCFCF;
        int binMid = 0xFF8A8A8A;

        // Bin shadow
        graphics.fill(x + 5, y + 7, x + 13, y + 8, shadow);
        graphics.fill(x + 6, y + 8, x + 12, y + 14, shadow);

        // Bin lid
        graphics.fill(x + 5, y + 5, x + 13, y + 6, binLight);
        graphics.fill(x + 7, y + 4, x + 11, y + 5, binLight);

        // Bin body outline
        graphics.fill(x + 6, y + 7, x + 12, y + 14, binDark);

        // Bin body fill
        graphics.fill(x + 7, y + 8, x + 11, y + 13, binMid);

        // Vertical grooves
        graphics.fill(x + 8, y + 8, x + 9, y + 13, binDark);
        graphics.fill(x + 10, y + 8, x + 11, y + 13, binDark);

        // Bottom lip
        graphics.fill(x + 7, y + 13, x + 11, y + 14, binLight);
    }

    public static void drawExtraStoragePanel(
            GuiGraphicsExtractor graphics,
            BackpackMenuLayout layout,
            int left,
            int top
    ) {
        int x = left + layout.extraStoragePanelX();
        int y = top + layout.extraStoragePanelY();
        int w = layout.extraStoragePanelWidth();
        int h = layout.extraStoragePanelHeight();

        drawSectionBox(graphics, x, y, w, h);
    }

    public static void drawSingleSlotFrame(
            GuiGraphicsExtractor graphics,
            int sx,
            int sy
    ) {
        drawSlotFrame(graphics, sx, sy);
    }

    public static void drawCraftingPanel(
            GuiGraphicsExtractor graphics,
            BackpackMenuLayout layout,
            int left,
            int top
    ) {
        int x = left + layout.craftingPanelX();
        int y = top + layout.craftingPanelY();
        int w = layout.craftingPanelWidth();
        int h = layout.craftingPanelHeight();

        drawThinFloatingPanel(graphics, x, y, w, h);
    }

    private static void drawThinFloatingPanel(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int w,
            int h
    ) {
        int outer = 0xCC151515;
        int inner = 0xAA242424;
        int highlight = 0xAA5A5A5A;
        int shadow = 0xCC101010;
        int tint = 0x55373737;

        graphics.fill(x, y, x + w, y + 1, outer);
        graphics.fill(x, y + h - 1, x + w, y + h, outer);
        graphics.fill(x, y, x + 1, y + h, outer);
        graphics.fill(x + w - 1, y, x + w, y + h, outer);

        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, tint);

        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, highlight);
        graphics.fill(x + 1, y + 1, x + 2, y + h - 1, highlight);

        graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, shadow);
        graphics.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, shadow);

        graphics.fill(x + 2, y + 2, x + w - 2, y + h - 2, inner);
    }
}