package com.anantaya.backpackpro.client;

import com.anantaya.backpackpro.backpack.BackpackMenuLayout;
import com.anantaya.backpackpro.backpack.BackpackScreenHandler;
import com.anantaya.backpackpro.client.gui.BackpackGuiButton;
import com.anantaya.backpackpro.client.gui.BackpackGuiRenderer;
import com.anantaya.backpackpro.upgrade.BackpackUpgradeConfigAction;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BackpackScreen extends AbstractContainerScreen<BackpackScreenHandler> {

    private final BackpackMenuLayout layout;
    private final List<BackpackGuiButton> buttons = new ArrayList<>();
    private final BackpackScreenUpgradePanel upgradePanel =
            new BackpackScreenUpgradePanel();

    private boolean clickedFluidStorageFakeSlot = false;
    private boolean clickedTrashSlot = false;

    public BackpackScreen(
            BackpackScreenHandler handler,
            Inventory inventory,
            Component title
    ) {
        super(
                handler,
                inventory,
                title,
                BackpackMenuLayout.of(handler.tier).guiWidth,
                BackpackMenuLayout.of(handler.tier).guiHeight
        );

        this.layout = BackpackMenuLayout.of(handler.tier);
    }

    @Override
    protected void init() {
        super.init();

        this.inventoryLabelY = layout.playerInvY - 11;
        this.buttons.clear();
    }

    @Override
    public void extractContents(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        int x = this.leftPos;
        int y = this.topPos;

        BackpackGuiRenderer.drawBase(graphics, layout, x, y);

        if (BackpackScreenPanels.hasExtraStoragePanel(this)) {
            BackpackGuiRenderer.drawExtraStoragePanel(
                    graphics,
                    layout,
                    x,
                    y
            );
        }

        BackpackScreenPanels.drawFluidStoragePanel(this, graphics);
        BackpackScreenPanels.drawTrashSlot(this, graphics);
        BackpackScreenPanels.drawCraftingPanel(this, graphics);

        BackpackScreenPanels.drawVisibleSlotFrames(this, graphics, x, y);
        BackpackGuiRenderer.drawLockedUpgradeSlots(graphics, layout, x, y);




        super.extractContents(graphics, mouseX, mouseY, delta);

        BackpackScreenButtonController.rebuildButtons(this, buttons);

        upgradePanel.draw(
                this,
                graphics,
                mouseX,
                mouseY
        );

        BackpackGuiRenderer.drawButtons(
                graphics,
                buttons,
                mouseX,
                mouseY,
                this.menu.tier
        );

        BackpackGuiRenderer.drawButtonTooltips(
                graphics,
                buttons,
                mouseX,
                mouseY
        );

        BackpackScreenPanels.drawTooltips(
                this,
                graphics,
                mouseX,
                mouseY
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (BackpackScreenPanels.handleFluidStorageFakeSlotClick(
                this,
                mouseX,
                mouseY
        )) {
            return true;
        }

        if (upgradePanel.mouseClicked(
                this,
                mouseX,
                mouseY
        )) {
            return true;
        }

        for (BackpackGuiButton button : buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                BackpackScreenButtonController.handleButtonClick(
                        this,
                        button
                );
                return true;
            }
        }

        if (BackpackScreenPanels.shouldShowTrashSlot()
                && BackpackScreenPanels.isInsideTrashSlot(
                this,
                event.x(),
                event.y()
        )) {
            BackpackScreenPanels.sendTrashClick(this);
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected boolean hasClickedOutside(
            double mouseX,
            double mouseY,
            int left,
            int top
    ) {
        if (BackpackScreenPanels.isInsideExtraStoragePanel(this, mouseX, mouseY)) {
            return false;
        }

        if (BackpackScreenPanels.isInsideFluidPanel(this, mouseX, mouseY)) {
            return false;
        }

        if (BackpackScreenPanels.isInsideCraftingPanel(this, mouseX, mouseY)) {
            return false;
        }

        if (BackpackScreenPanels.shouldShowTrashSlot()
                && BackpackScreenPanels.isInsideTrashSlot(this, mouseX, mouseY)) {
            return false;
        }

        return super.hasClickedOutside(mouseX, mouseY, left, top);
    }

    @Override
    protected void extractLabels(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        // No text labels. Sleek icon-based GUI.
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (clickedFluidStorageFakeSlot) {
            clickedFluidStorageFakeSlot = false;
            return true;
        }

        if (clickedTrashSlot) {
            clickedTrashSlot = false;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_B) {
            if (this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }

            return true;
        }

        return super.keyPressed(event);
    }

    public BackpackMenuLayout layout() {
        return layout;
    }

    public BackpackScreenHandler handler() {
        return this.menu;
    }

    public int screenLeft() {
        return this.leftPos;
    }

    public int screenTop() {
        return this.topPos;
    }

    public BackpackScreenUpgradePanel upgradePanel() {
        return upgradePanel;
    }

    public void quickMoveSlot(Slot slot) {
        this.slotClicked(
                slot,
                slot.index,
                0,
                ContainerInput.QUICK_MOVE
        );
    }

    public void sendConfigAction(
            int upgradeSlotIndex,
            BackpackUpgradeConfigAction action
    ) {
        BackpackScreenPanels.sendConfigAction(
                upgradeSlotIndex,
                action
        );
    }

    public void showTooltip(
            GuiGraphicsExtractor graphics,
            List<Component> lines,
            int mouseX,
            int mouseY
    ) {
        graphics.setComponentTooltipForNextFrame(
                this.font,
                lines,
                mouseX,
                mouseY
        );
    }

    public void markFluidStorageFakeSlotClicked() {
        this.clickedFluidStorageFakeSlot = true;
    }

    public void markTrashSlotClicked() {
        this.clickedTrashSlot = true;
    }
}