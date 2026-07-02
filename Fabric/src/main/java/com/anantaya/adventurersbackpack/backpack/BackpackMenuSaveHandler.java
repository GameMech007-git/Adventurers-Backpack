package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.block.entity.BackpackBlockEntity;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseMenuHandler;
import com.anantaya.adventurersbackpack.upgrade.crafting.BackpackCraftingMenuHandler;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.ExtraStorageMenuHandler;
import com.anantaya.adventurersbackpack.upgrade.nested.NestedUpgradeMenuHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BackpackMenuSaveHandler {

    private BackpackMenuSaveHandler() {
    }

    public static void removed(
            Player player,
            Container inventory,
            ItemStack backpackStack,
            BackpackBlockEntity blockEntity,
            BackpackTier tier,
            ExtraStorageMenuHandler extraStorageMenuHandler,
            CartographersCaseMenuHandler cartographersCaseMenuHandler,
            NestedUpgradeMenuHandler nestedUpgradeMenuHandler,
            BackpackCraftingMenuHandler craftingMenuHandler
    ) {
        if (inventory instanceof BackpackInventory backpackInventory) {
            backpackInventory.saveToData();

            cartographersCaseMenuHandler.saveToBackpack();
            nestedUpgradeMenuHandler.saveToBackpack();
            extraStorageMenuHandler.setChanged();

            if (!player.level().isClientSide()) {
                BackpackUpgradeHelper.hasUpgrade(
                        backpackStack,
                        BackpackUpgradeItem.Type.LANTERN_HOOK,
                        tier,
                        player.level().registryAccess()
                );
            }
        } else {
            inventory.setChanged();
            extraStorageMenuHandler.setChanged();
            cartographersCaseMenuHandler.saveToBackpack();
            nestedUpgradeMenuHandler.saveToBackpack();

            if (blockEntity != null) {
                blockEntity.loadCartographersCaseFromBackpackStack(backpackStack);
                blockEntity.loadNestedUpgradeFromBackpackStack(backpackStack);
                blockEntity.setChanged();
            }

            if (!player.level().isClientSide()) {
                BackpackUpgradeHelper.hasUpgrade(
                        inventory,
                        BackpackUpgradeItem.Type.LANTERN_HOOK,
                        tier
                );
            }
        }

        craftingMenuHandler.removed(player);
    }
}