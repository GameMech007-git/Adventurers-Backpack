package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.menu.BackpackSlotRules;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BackpackMenuTransferHelper {

    private final BackpackScreenHandler menu;
    private final BackpackTier tier;
    private final Container extraStorageInventory;
    private final int extraStorageSlots;
    private final ExtraStorageAccess extraStorageAccess;

    private static final int CRAFTING_INPUT_SLOTS = 9;
    private static final int CRAFTING_RESULT_SLOTS = 1;

    public BackpackMenuTransferHelper(
            BackpackScreenHandler menu,
            BackpackTier tier,
            Container extraStorageInventory,
            int extraStorageSlots,
            ExtraStorageAccess extraStorageAccess
    ) {
        this.menu = menu;
        this.tier = tier;
        this.extraStorageInventory = extraStorageInventory;
        this.extraStorageSlots = extraStorageSlots;
        this.extraStorageAccess = extraStorageAccess;
    }

    public boolean moveFromBackpackToPlayer(ItemStack stackInSlot) {
        return menu.moveStackToRange(
                stackInSlot,
                playerInventoryStart(),
                menu.slots.size(),
                true
        );
    }

    public boolean moveFromExtraStorageToPlayer(ItemStack stackInSlot) {
        boolean moved = menu.moveStackToRange(
                stackInSlot,
                playerInventoryStart(),
                menu.slots.size(),
                true
        );

        if (moved && extraStorageInventory != null) {
            extraStorageInventory.setChanged();
        }

        return moved;
    }

    public boolean moveFromPlayerToBackpack(ItemStack stackInSlot) {
        if (stackInSlot.getItem() instanceof BackpackUpgradeItem) {
            return menu.moveStackToRange(
                    stackInSlot,
                    tier.upgradeStart(),
                    tier.totalSlots,
                    false
            );
        }

        return moveIntoBackpackStoragePriority(stackInSlot);
    }

    private boolean moveIntoBackpackStoragePriority(ItemStack stack) {
        if (!BackpackSlotRules.mayPlaceInNormalSlot(stack)) {
            return false;
        }

        if (menu.moveStackToRange(
                stack,
                0,
                tier.protectedSlots,
                false
        )) {
            return true;
        }

        if (menu.moveStackToRange(
                stack,
                tier.normalStart(),
                tier.upgradeStart(),
                false
        )) {
            return true;
        }

        if (extraStorageAccess.canUseExtraStorageSlots()) {
            return menu.moveStackToRange(
                    stack,
                    extraStorageStart(),
                    extraStorageEnd(),
                    false
            );
        }

        return false;
    }

    public boolean movePlayerInventoryToBackpackSkippingHotbar() {
        boolean changed = false;

        for (int index = playerMainInventoryStart(); index < playerMainInventoryEnd(); index++) {
            if (moveSinglePlayerSlotToBackpack(index)) {
                changed = true;
            }
        }

        return changed;
    }

    public boolean moveBackpackToPlayerInventoryIncludingHotbar() {
        boolean changed = false;

        for (int index = 0; index < tier.protectedSlots; index++) {
            if (moveSingleBackpackSlotToPlayer(index)) {
                changed = true;
            }
        }


        for (int index = tier.normalStart(); index < tier.upgradeStart(); index++) {
            if (moveSingleBackpackSlotToPlayer(index)) {
                changed = true;
            }
        }

        if (extraStorageAccess.canUseExtraStorageSlots()) {
            for (int index = extraStorageStart(); index < extraStorageEnd(); index++) {
                if (moveSingleBackpackSlotToPlayer(index)) {
                    changed = true;
                }
            }

            if (changed && extraStorageInventory != null) {
                extraStorageInventory.setChanged();
            }
        }

        return changed;
    }

    private boolean moveSinglePlayerSlotToBackpack(int index) {
        if (index < 0 || index >= menu.slots.size()) {
            return false;
        }

        Slot slot = menu.slots.get(index);

        if (!slot.hasItem()) {
            return false;
        }

        ItemStack stackInSlot = slot.getItem();

        if (stackInSlot.isEmpty()) {
            return false;
        }

        if (stackInSlot.getItem() instanceof BackpackItem) {
            return false;
        }

        int oldCount = stackInSlot.getCount();

        boolean moved = moveFromPlayerToBackpack(stackInSlot);

        if (!moved) {
            return false;
        }

        if (stackInSlot.getCount() == oldCount) {
            return false;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return true;
    }

    private boolean moveSingleBackpackSlotToPlayer(int index) {
        if (index < 0 || index >= menu.slots.size()) {
            return false;
        }

        Slot slot = menu.slots.get(index);

        if (!slot.hasItem()) {
            return false;
        }

        ItemStack stackInSlot = slot.getItem();

        if (stackInSlot.isEmpty()) {
            return false;
        }

        if (stackInSlot.getItem() instanceof BackpackUpgradeItem) {
            return false;
        }

        int oldCount = stackInSlot.getCount();

        boolean moved = menu.moveStackToRange(
                stackInSlot,
                playerInventoryStart(),
                menu.slots.size(),
                false
        );

        if (!moved) {
            return false;
        }

        if (stackInSlot.getCount() == oldCount) {
            return false;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return true;
    }

    public int extraStorageStart() {
        return tier.totalSlots;
    }

    public int extraStorageEnd() {
        return extraStorageStart() + extraStorageSlots;
    }

    public int playerMainInventoryStart() {
        return playerInventoryStart();
    }

    public int playerMainInventoryEnd() {
        return playerMainInventoryStart() + 27;
    }

    public int hotbarStart() {
        return playerMainInventoryEnd();
    }

    public int hotbarEnd() {
        return hotbarStart() + 9;
    }

    public boolean isExtraStorageMenuSlot(int index) {
        return index >= extraStorageStart() && index < extraStorageEnd();
    }

    public boolean isBackpackMainSlot(int index) {
        return index >= 0 && index < tier.totalSlots;
    }

    public interface ExtraStorageAccess {
        boolean canUseExtraStorageSlots();
    }

    public int craftingInputStart() {
        return extraStorageEnd();
    }

    public int craftingInputEnd() {
        return craftingInputStart() + CRAFTING_INPUT_SLOTS;
    }

    public int craftingResultSlot() {
        return craftingInputEnd();
    }

    public int craftingEnd() {
        return craftingResultSlot() + CRAFTING_RESULT_SLOTS;
    }

    public int playerInventoryStart() {
        return craftingEnd();
    }

    public boolean isCraftingInputSlot(int index) {
        return index >= craftingInputStart()
                && index < craftingInputEnd();
    }

    public boolean isCraftingResultSlot(int index) {
        return index == craftingResultSlot();
    }

    public boolean isCraftingSlot(int index) {
        return isCraftingInputSlot(index)
                || isCraftingResultSlot(index);
    }
}