package com.anantaya.adventurersbackpack.upgrade.nested;

import com.anantaya.adventurersbackpack.backpack.BackpackScreenHandler;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class NestedUpgradeMenuHandler {

    private final BackpackScreenHandler menu;
    private final Container backpackInventory;
    private final Container nestedInventory;
    private final ItemStack backpackStack;
    private final BackpackTier tier;

    public NestedUpgradeMenuHandler(
            BackpackScreenHandler menu,
            Container backpackInventory,
            Container nestedInventory,
            ItemStack backpackStack,
            BackpackTier tier
    ) {
        this.menu = menu;
        this.backpackInventory = backpackInventory;
        this.nestedInventory = nestedInventory;
        this.backpackStack = backpackStack;
        this.tier = tier;
    }

    public boolean hasUpgrade() {
        return BackpackUpgradeHelper.hasUpgrade(
                backpackInventory,
                BackpackUpgradeItem.Type.NESTED_UPGRADE,
                tier
        );
    }

    public boolean hasAnyStoredItem() {
        for (int i = 0; i < nestedInventory.getContainerSize(); i++) {
            if (!nestedInventory.getItem(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public void saveToBackpack() {
        if (nestedInventory instanceof NestedUpgradeInventory inventory) {
            inventory.saveToBackpack();
        }
    }
}