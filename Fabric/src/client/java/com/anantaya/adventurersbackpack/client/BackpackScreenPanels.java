package com.anantaya.adventurersbackpack.client;

import com.anantaya.adventurersbackpack.client.gui.BackpackGuiRenderer;
import com.anantaya.adventurersbackpack.network.BackpackTrashPayload;
import com.anantaya.adventurersbackpack.network.FluidStoragePayload;
import com.anantaya.adventurersbackpack.network.UpgradeConfigPayload;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.fluidstorage.BackpackFluidStorageHelper;
import com.anantaya.adventurersbackpack.upgrade.fluidstorage.FluidStorageType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class BackpackScreenPanels {

    private static final int TRASH_SIZE = 18;
    private static final int FLUID_PANEL_WIDTH = 30;
    private static int fluidPanelHeight(BackpackScreen screen) {
        int capacity = BackpackFluidStorageHelper.capacityForTier(screen.handler().tier);

        int barTopFromPanelTop = 30;
        int segmentH = 6;
        int segmentGap = 1;
        int bottomPadding = 8;

        int barH = capacity * segmentH + (capacity - 1) * segmentGap;

        return barTopFromPanelTop + barH + bottomPadding;
    }

    private BackpackScreenPanels() {
    }

    public static void drawFluidStoragePanel(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics
    ) {
        if (hasFluidStorageUpgrade(screen)) {
            return;
        }

        FluidStorageType type = screen.handler().getSyncedFluidType();
        int amount = screen.handler().getSyncedFluidAmount();

        BackpackGuiRenderer.drawFluidStoragePanel(
                graphics,
                getFluidPanelX(screen),
                getFluidPanelY(screen),
                type,
                amount,
                BackpackFluidStorageHelper.capacityForTier(screen.handler().tier),
                fluidPanelHeight(screen)
        );
    }

    public static void drawTrashSlot(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics
    ) {
        if (!shouldShowTrashSlot()) {
            return;
        }

        BackpackGuiRenderer.drawTrashSlot(
                graphics,
                getTrashX(screen),
                getTrashY(screen)
        );
    }

    public static void drawVisibleSlotFrames(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        for (int i = 0; i < screen.handler().slots.size(); i++) {
            Slot slot = screen.handler().slots.get(i);

            if (!slot.isActive()) {
                continue;
            }

            if (screen.handler().isExtraStorageMenuSlot(i)
                    && !hasExtraStoragePanel(screen)) {
                continue;
            }

            if (screen.handler().isCraftingMenuSlot(i)
                    && screen.handler().hasCraftingUpgradeSynced()) {
                continue;
            }

            BackpackGuiRenderer.drawSingleSlotFrame(
                    graphics,
                    x + slot.x - 1,
                    y + slot.y - 1
            );
        }
    }

    public static void drawTooltips(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        drawFluidStorageTooltip(
                screen,
                graphics,
                mouseX,
                mouseY
        );

        drawExtraStorageTooltip(
                screen,
                graphics,
                mouseX,
                mouseY
        );

        drawBlockedCraftingUpgradeTooltip(
                screen,
                graphics,
                mouseX,
                mouseY
        );

        drawBlockedExtraStorageUpgradeTooltip(
                screen,
                graphics,
                mouseX,
                mouseY
        );

        drawBlockedCartographersCaseUpgradeTooltip(
                screen,
                graphics,
                mouseX,
                mouseY
        );
    }

    public static boolean handleFluidStorageFakeSlotClick(
            BackpackScreen screen,
            int mouseX,
            int mouseY
    ) {
        if (hasFluidStorageUpgrade(screen)) {
            return false;
        }

        int slotX = getFluidPanelX(screen) + 6;
        int slotY = getFluidPanelY(screen) + 6;

        boolean hoveringFakeSlot =
                mouseX >= slotX
                        && mouseX < slotX + 18
                        && mouseY >= slotY
                        && mouseY < slotY + 18;

        if (!hoveringFakeSlot) {
            return false;
        }

        screen.markFluidStorageFakeSlotClicked();
        ClientPlayNetworking.send(new FluidStoragePayload());
        return true;
    }

    public static void sendTrashClick(BackpackScreen screen) {
        ClientPlayNetworking.send(new BackpackTrashPayload());
        screen.markTrashSlotClicked();
    }

    public static void sendConfigAction(
            int upgradeSlotIndex,
            BackpackUpgradeConfigAction action
    ) {
        ClientPlayNetworking.send(
                new UpgradeConfigPayload(
                        upgradeSlotIndex,
                        action
                )
        );
    }

    public static boolean hasExtraStoragePanel(BackpackScreen screen) {
        return screen.handler().hasExtraStorageUpgradeSynced();
    }

    public static boolean hasFluidStorageUpgrade(BackpackScreen screen) {
        int start = screen.handler().tier.upgradeStart();
        int end = screen.handler().tier.totalSlots;

        for (int i = start; i < end && i < screen.handler().slots.size(); i++) {
            Slot slot = screen.handler().slots.get(i);

            if (!slot.hasItem()) {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (stack.getItem() instanceof BackpackUpgradeItem upgradeItem
                    && upgradeItem.getType() == BackpackUpgradeItem.Type.FLUID_STORAGE) {
                return false;
            }
        }

        return true;
    }

    public static boolean shouldShowTrashSlot() {
        return true;
    }

    public static boolean isInsideTrashSlot(
            BackpackScreen screen,
            double mouseX,
            double mouseY
    ) {
        int x = getTrashX(screen);
        int y = getTrashY(screen);

        return mouseX >= x
                && mouseX < x + TRASH_SIZE
                && mouseY >= y
                && mouseY < y + TRASH_SIZE;
    }

    public static boolean isInsideExtraStoragePanel(
            BackpackScreen screen,
            double mouseX,
            double mouseY
    ) {
        if (!hasExtraStoragePanel(screen)) {
            return false;
        }

        int x = screen.screenLeft() + screen.layout().extraStoragePanelX();
        int y = screen.screenTop() + screen.layout().extraStoragePanelY();
        int width = screen.layout().extraStoragePanelWidth();
        int height = screen.layout().extraStoragePanelHeight();

        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    public static boolean isInsideFluidPanel(
            BackpackScreen screen,
            double mouseX,
            double mouseY
    ) {
        if (hasFluidStorageUpgrade(screen)) {
            return false;
        }

        int x = getFluidPanelX(screen);
        int y = getFluidPanelY(screen);

        return mouseX >= x
                && mouseX < x + FLUID_PANEL_WIDTH
                && mouseY >= y
                && mouseY < y + fluidPanelHeight(screen);
    }

    private static void drawFluidStorageTooltip(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        if (hasFluidStorageUpgrade(screen)) {
            return;
        }

        int panelX = getFluidPanelX(screen);
        int panelY = getFluidPanelY(screen);

        boolean hoveringPanel =
                mouseX >= panelX
                        && mouseX < panelX + FLUID_PANEL_WIDTH
                        && mouseY >= panelY
                        && mouseY < panelY + fluidPanelHeight(screen);

        if (!hoveringPanel) {
            return;
        }

        FluidStorageType type = screen.handler().getSyncedFluidType();
        int amount = screen.handler().getSyncedFluidAmount();
        int capacity = BackpackFluidStorageHelper.capacityForTier(screen.handler().tier);

        Component fluidLine = Component.literal("Stored: "
                + formatFluidName(type)
                + " "
                + amount
                + " / "
                + capacity
                + " buckets");

        Component statusLine;

        if (type == FluidStorageType.NONE || amount <= 0) {
            statusLine = Component.literal("Tank is empty");
        } else if (amount >= capacity) {
            statusLine = Component.literal("Tank is full");
        } else {
            statusLine = Component.literal("Can accept only "
                    + formatFluidName(type));
        }

        Component actionLine = Component.literal(getFluidStorageActionHint(screen));

        screen.showTooltip(
                graphics,
                List.of(
                        Component.literal("Fluid Storage"),
                        fluidLine,
                        statusLine,
                        actionLine,
                        Component.literal("Only one fluid type at a time")
                ),
                mouseX,
                mouseY
        );
    }

    private static void drawExtraStorageTooltip(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        if (!hasExtraStoragePanel(screen)) {
            return;
        }

        int panelX = screen.screenLeft() + screen.layout().extraStoragePanelX();
        int panelY = screen.screenTop() + screen.layout().extraStoragePanelY();
        int panelW = screen.layout().extraStoragePanelWidth();
        int panelH = screen.layout().extraStoragePanelHeight();

        boolean hoveringPanel =
                mouseX >= panelX
                        && mouseX < panelX + panelW
                        && mouseY >= panelY
                        && mouseY < panelY + panelH;

        if (!hoveringPanel) {
            return;
        }

        screen.showTooltip(
                graphics,
                List.of(
                        Component.literal("Extra Storage"),
                        Component.literal("Automation ignore this panel")
                ),
                mouseX,
                mouseY
        );
    }

    private static void drawBlockedCartographersCaseUpgradeTooltip(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        if (!screen.handler().hasAnyCartographersCaseItem()) {
            return;
        }

        int start = screen.handler().tier.upgradeStart();
        int end = screen.handler().tier.totalSlots;

        for (int i = start; i < end && i < screen.handler().slots.size(); i++) {
            Slot slot = screen.handler().slots.get(i);

            int slotX = screen.screenLeft() + slot.x;
            int slotY = screen.screenTop() + slot.y;

            boolean hovering =
                    mouseX >= slotX
                            && mouseX < slotX + 18
                            && mouseY >= slotY
                            && mouseY < slotY + 18;

            if (!hovering) {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (!(stack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
                continue;
            }

            if (upgradeItem.getType() != BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE) {
                continue;
            }

            screen.showTooltip(
                    graphics,
                    List.of(
                            Component.literal("Cartographer's Case Upgrade"),
                            Component.literal("Compass slots must be empty!")
                                    .withStyle(ChatFormatting.RED)
                    ),
                    mouseX,
                    mouseY
            );

            return;
        }
    }

    private static int getTrashX(BackpackScreen screen) {
        return screen.screenLeft() + screen.layout().trashSlotX();
    }

    private static int getTrashY(BackpackScreen screen) {
        return screen.screenTop() + screen.layout().trashSlotY();
    }

    private static int getFluidPanelX(BackpackScreen screen) {
        return screen.screenLeft() - 36;
    }

    private static int getFluidPanelY(BackpackScreen screen) {
        return screen.screenTop();
    }

    private static String getFluidStorageActionHint(BackpackScreen screen) {
        ItemStack carried = screen.handler().getCarried();

        if (carried.isEmpty()) {
            return "Hold a bucket to insert or withdraw";
        }

        if (carried.is(Items.WATER_BUCKET)) {
            return "Click to store 1 water bucket";
        }

        if (carried.is(Items.LAVA_BUCKET)) {
            return "Click to store 1 lava bucket";
        }

        if (carried.is(Items.MILK_BUCKET)) {
            return "Click to store 1 milk bucket";
        }

        if (carried.is(Items.BUCKET)) {
            return "Click to fill an empty bucket";
        }

        return "Unsupported item";
    }

    private static String formatFluidName(FluidStorageType type) {
        return switch (type) {
            case WATER -> "Water";
            case LAVA -> "Lava";
            case MILK -> "Milk";
            case NONE -> "Empty";
        };
    }

    public static void drawCraftingPanel(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics
    ) {
        if (screen.handler().hasCraftingUpgradeSynced()) {
            return;
        }

        BackpackGuiRenderer.drawCraftingPanel(
                graphics,
                screen.layout(),
                screen.screenLeft(),
                screen.screenTop()
        );
    }

    public static boolean isInsideCraftingPanel(
            BackpackScreen screen,
            double mouseX,
            double mouseY
    ) {
        if (screen.handler().hasCraftingUpgradeSynced()) {
            return false;
        }

        int x = screen.screenLeft() + screen.layout().craftingPanelX();
        int y = screen.screenTop() + screen.layout().craftingPanelY();
        int w = screen.layout().craftingPanelWidth();
        int h = screen.layout().craftingPanelHeight();

        return mouseX >= x
                && mouseX < x + w
                && mouseY >= y
                && mouseY < y + h;
    }

    private static void drawBlockedExtraStorageUpgradeTooltip(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        if (!screen.handler().hasAnyExtraStorageItem()) {
            return;
        }

        int start = screen.handler().tier.upgradeStart();
        int end = screen.handler().tier.totalSlots;

        for (int i = start; i < end && i < screen.handler().slots.size(); i++) {
            Slot slot = screen.handler().slots.get(i);

            int slotX = screen.screenLeft() + slot.x;
            int slotY = screen.screenTop() + slot.y;

            boolean hovering =
                    mouseX >= slotX
                            && mouseX < slotX + 18
                            && mouseY >= slotY
                            && mouseY < slotY + 18;

            if (!hovering) {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (!(stack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
                continue;
            }

            if (upgradeItem.getType() != BackpackUpgradeItem.Type.EXTRA_STORAGE) {
                continue;
            }

            screen.showTooltip(
                    graphics,
                    List.of(
                            Component.literal("Extra Storage Upgrade"),
                            Component.literal("Grid must be empty!")
                                    .withStyle(ChatFormatting.RED)
                    ),
                    mouseX,
                    mouseY
            );

            return;
        }
    }

    private static void drawBlockedCraftingUpgradeTooltip(
            BackpackScreen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        if (!screen.handler().hasAnyCraftingInputItem()) {
            return;
        }

        int start = screen.handler().tier.upgradeStart();
        int end = screen.handler().tier.totalSlots;

        for (int i = start; i < end && i < screen.handler().slots.size(); i++) {
            Slot slot = screen.handler().slots.get(i);

            int slotX = screen.screenLeft() + slot.x;
            int slotY = screen.screenTop() + slot.y;

            boolean hovering =
                    mouseX >= slotX
                            && mouseX < slotX + 18
                            && mouseY >= slotY
                            && mouseY < slotY + 18;

            if (!hovering) {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (!(stack.getItem() instanceof BackpackUpgradeItem upgradeItem)) {
                continue;
            }

            if (upgradeItem.getType() != BackpackUpgradeItem.Type.CRAFTING) {
                continue;
            }

            screen.showTooltip(
                    graphics,
                    List.of(
                            Component.literal("Crafting Upgrade"),
                            Component.literal("Grid must be empty!")
                                    .withStyle(ChatFormatting.RED)
                    ),
                    mouseX,
                    mouseY
            );

            return;
        }


    }


}