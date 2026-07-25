package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.menu.BackpackSlotRules;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.world.Container;
import com.anantaya.adventurersbackpack.upgrade.nested.NestedUpgradeData;
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
        if (stackInSlot.isEmpty()) {
            return false;
        }

        if (stackInSlot.getItem() instanceof BackpackUpgradeItem) {
            return moveIntoPreferredRanges(
                    stackInSlot,
                    getPreferredTransferRanges(stackInSlot, tier, extraStorageSlots, true)
            );
        }

        return moveIntoBackpackStoragePriority(stackInSlot);
    }

    private boolean moveIntoBackpackStoragePriority(ItemStack stack) {
        if (!BackpackSlotRules.mayPlaceInNormalSlot(stack)) {
            return false;
        }

        return moveIntoPreferredRanges(
                stack,
                getPreferredTransferRanges(
                        stack,
                        tier,
                        extraStorageSlots,
                        extraStorageAccess.canUseExtraStorageSlots()
                )
        );
    }

    private boolean moveIntoPreferredRanges(ItemStack stack, int[][] ranges) {
        if (stack.isEmpty()) {
            return false;
        }

        int originalCount = stack.getCount();
        boolean moved = false;

        for (int[] range : ranges) {
            if (stack.isEmpty() || range == null || range.length != 2) {
                break;
            }

            int start = range[0];
            int end = range[1];

            if (start >= end) {
                continue;
            }

            if (menu.moveStackToRange(stack, start, end, false)) {
                if (stack.getCount() < originalCount) {
                    moved = true;
                    originalCount = stack.getCount();
                }
            }
        }

        return moved;
    }

    static int[][] getPreferredTransferRanges(
            ItemStack stack,
            BackpackTier tier,
            int extraStorageSlots,
            boolean includeExtraStorage
    ) {
        if (stack.isEmpty()) {
            return new int[0][];
        }

        if (stack.getItem() instanceof BackpackUpgradeItem) {
            return new int[][] {
                    {tier.upgradeStart(), tier.upgradeStart() + tier.upgradeSlots}
            };
        }

        int[][] ranges = new int[includeExtraStorage ? 3 : 2][];
        ranges[0] = new int[] {0, tier.protectedSlots};
        ranges[1] = new int[] {tier.normalStart(), tier.upgradeStart()};

        if (includeExtraStorage) {
            ranges[2] = new int[] {tier.totalSlots, tier.totalSlots + extraStorageSlots};
        }

        return ranges;
    }

    public boolean movePlayerInventoryToBackpackSkippingHotbar() {
        boolean changed = false;

        for (int playerInventorySlot = 9; playerInventorySlot < 36; playerInventorySlot++) {
            int menuIndex = playerInventoryMenuIndex(playerInventorySlot);

            if (menuIndex < 0) {
                continue;
            }

            if (moveSinglePlayerSlotToBackpack(menuIndex)) {
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

        if (stackInSlot.isEmpty() || stackInSlot.getItem() instanceof BackpackUpgradeItem) {
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

    int playerInventoryMenuIndex(int playerInventorySlot) {
        if (playerInventorySlot < 9 || playerInventorySlot >= 36) {
            return -1;
        }

        return playerMainInventoryStart() + (playerInventorySlot - 9);
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
        // Player inventory slots are added after crafting, cartographers case, and nested upgrade slots.
        // Crafting slots (inputs + result) are counted by craftingEnd(). Cartographers case adds 8
        // navigation slots and nested upgrades add NestedUpgradeData.SLOT_COUNT slots.
        return craftingEnd() + 8 + NestedUpgradeData.SLOT_COUNT;
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