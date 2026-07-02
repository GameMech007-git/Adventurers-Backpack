package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.upgrade.crafting.BackpackCraftingMenuHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BackpackMenuQuickMoveHandler {

    private BackpackMenuQuickMoveHandler() {
    }

    public static ItemStack quickMoveStack(
            BackpackScreenHandler menu,
            BackpackMenuTransferHelper transferHelper,
            BackpackCraftingMenuHandler craftingMenuHandler,
            Player player,
            int index
    ) {
        Slot slot = menu.slots.get(index);

        if (!slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();

        if (stackInSlot.getItem() instanceof BackpackItem) {
            return ItemStack.EMPTY;
        }

        ItemStack result = stackInSlot.copy();

        boolean moved;

        if (transferHelper.isBackpackMainSlot(index)) {
            moved = transferHelper.moveFromBackpackToPlayer(stackInSlot);
        } else if (transferHelper.isExtraStorageMenuSlot(index)) {
            moved = transferHelper.moveFromExtraStorageToPlayer(stackInSlot);
        } else if (transferHelper.isCraftingResultSlot(index)) {
            moved = menu.moveStackToRange(
                    stackInSlot,
                    transferHelper.playerInventoryStart(),
                    menu.slots.size(),
                    true
            );
        } else if (transferHelper.isCraftingInputSlot(index)) {
            moved = menu.moveStackToRange(
                    stackInSlot,
                    transferHelper.playerInventoryStart(),
                    menu.slots.size(),
                    false
            );
        } else {
            moved = transferHelper.moveFromPlayerToBackpack(stackInSlot);
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackInSlot);

        if (transferHelper.isCraftingResultSlot(index)) {
            craftingMenuHandler.updateCraftingResult(player);
        }

        return result;
    }
}