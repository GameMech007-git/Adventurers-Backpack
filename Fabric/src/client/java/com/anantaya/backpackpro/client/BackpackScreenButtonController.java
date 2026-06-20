package com.anantaya.backpackpro.client;

import com.anantaya.backpackpro.backpack.BackpackMenuLayout;
import com.anantaya.backpackpro.client.gui.BackpackGuiButton;
import com.anantaya.backpackpro.client.gui.BackpackGuiButtonType;
import com.anantaya.backpackpro.client.gui.BackpackGuiIcons;
import com.anantaya.backpackpro.network.BackpackSortPayload;
import com.anantaya.backpackpro.upgrade.BackpackUpgradeConfigAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public final class BackpackScreenButtonController {

    private BackpackScreenButtonController() {
    }

    public static void rebuildButtons(
            BackpackScreen screen,
            List<BackpackGuiButton> buttons
    ) {
        buttons.clear();

        int baseX = screen.screenLeft();
        int baseY = screen.screenTop();

        addTopButtons(screen, buttons, baseX, baseY);
        addUpgradeGearButtons(screen, buttons, baseX, baseY);
    }

    public static void handleButtonClick(
            BackpackScreen screen,
            BackpackGuiButton button
    ) {
        switch (button.type) {
            case SORT -> ClientPlayNetworking.send(new BackpackSortPayload());

            case MOVE_TO_PLAYER ->
                    BackpackScreenTransferActions.moveBackpackStorageToPlayer(screen);

            case MOVE_TO_BACKPACK ->
                    BackpackScreenTransferActions.movePlayerInventoryToBackpack(screen);

            case UPGRADE_CONFIG ->
                    screen.upgradePanel().toggleSelectedUpgradeSlot(button.data);

            case AUTO_PICKUP_MODE -> screen.sendConfigAction(
                    button.data,
                    BackpackUpgradeConfigAction.CYCLE_AUTO_PICKUP_MODE
            );

            case AUTO_PICKUP_IGNORE_DROPS -> screen.sendConfigAction(
                    button.data,
                    BackpackUpgradeConfigAction.TOGGLE_IGNORE_PLAYER_DROPS
            );
        }
    }

    private static void addTopButtons(
            BackpackScreen screen,
            List<BackpackGuiButton> buttons,
            int baseX,
            int baseY
    ) {
        buttons.add(new BackpackGuiButton(
                BackpackGuiButtonType.SORT,
                baseX + BackpackMenuLayout.SORT_BUTTON_X,
                baseY + BackpackMenuLayout.TOP_BUTTON_Y,
                BackpackMenuLayout.TOP_BUTTON_SIZE,
                BackpackGuiIcons.SORT,
                Component.literal("Sort normal storage")
        ));

        buttons.add(new BackpackGuiButton(
                BackpackGuiButtonType.MOVE_TO_PLAYER,
                baseX + BackpackMenuLayout.DOWN_BUTTON_X,
                baseY + BackpackMenuLayout.TOP_BUTTON_Y,
                BackpackMenuLayout.TOP_BUTTON_SIZE,
                BackpackGuiIcons.ARROW_DOWN,
                Component.literal("Move backpack storage to inventory")
        ));

        buttons.add(new BackpackGuiButton(
                BackpackGuiButtonType.MOVE_TO_BACKPACK,
                baseX + BackpackMenuLayout.UP_BUTTON_X,
                baseY + BackpackMenuLayout.TOP_BUTTON_Y,
                BackpackMenuLayout.TOP_BUTTON_SIZE,
                BackpackGuiIcons.ARROW_UP,
                Component.literal("Move inventory to backpack storage")
        ));
    }

    private static void addUpgradeGearButtons(
            BackpackScreen screen,
            List<BackpackGuiButton> buttons,
            int baseX,
            int baseY
    ) {
        for (int i = 0; i < screen.handler().tier.upgradeSlots; i++) {
            int slotIndex = screen.handler().tier.upgradeStart() + i;

            if (slotIndex < 0 || slotIndex >= screen.handler().slots.size()) {
                continue;
            }

            Slot slot = screen.handler().slots.get(slotIndex);

            if (!BackpackScreenUpgradePanel.isConfigurableUpgradeSlot(slot)) {
                continue;
            }

            buttons.add(new BackpackGuiButton(
                    BackpackGuiButtonType.UPGRADE_CONFIG,
                    baseX + screen.layout().upgradeSlotX() + 20,
                    baseY + screen.layout().upgradeSlotY(i) + 5,
                    8,
                    BackpackGuiIcons.GEAR,
                    Component.literal("Configure upgrade"),
                    slotIndex
            ));
        }
    }
}