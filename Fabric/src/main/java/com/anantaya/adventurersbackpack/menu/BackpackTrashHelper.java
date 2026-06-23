package com.anantaya.adventurersbackpack.menu;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class BackpackTrashHelper {

    private BackpackTrashHelper() {
    }

    public static void trashCarriedStack(AbstractContainerMenu menu) {
        if (menu.getCarried().isEmpty()) {
            return;
        }

        menu.setCarried(ItemStack.EMPTY);
        menu.broadcastChanges();
    }
}