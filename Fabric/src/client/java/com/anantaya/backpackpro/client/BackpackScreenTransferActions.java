package com.anantaya.backpackpro.client;

import net.minecraft.world.inventory.Slot;

public final class BackpackScreenTransferActions {

    private BackpackScreenTransferActions() {
    }

    public static void moveBackpackStorageToPlayer(BackpackScreen screen) {
        moveSlotRangeToPlayer(
                screen,
                0,
                screen.handler().tier.protectedSlots
        );

        moveSlotRangeToPlayer(
                screen,
                screen.handler().tier.normalStart(),
                screen.handler().tier.upgradeStart()
        );

        if (screen.handler().hasExtraStorageUpgradeSynced()) {
            moveSlotRangeToPlayer(
                    screen,
                    screen.handler().extraStorageStart(),
                    screen.handler().extraStorageEnd()
            );
        }
    }

    public static void movePlayerInventoryToBackpack(BackpackScreen screen) {
        int start = screen.handler().playerMainInventoryStart();
        int end = screen.handler().playerMainInventoryEnd();

        for (int i = start; i < end && i < screen.handler().slots.size(); i++) {
            Slot slot = screen.handler().slots.get(i);

            if (!slot.hasItem()) {
                continue;
            }

            screen.quickMoveSlot(slot);
        }
    }

    private static void moveSlotRangeToPlayer(
            BackpackScreen screen,
            int start,
            int end
    ) {
        for (int i = start; i < end && i < screen.handler().slots.size(); i++) {
            Slot slot = screen.handler().slots.get(i);

            if (!slot.hasItem()) {
                continue;
            }

            screen.quickMoveSlot(slot);
        }
    }
}