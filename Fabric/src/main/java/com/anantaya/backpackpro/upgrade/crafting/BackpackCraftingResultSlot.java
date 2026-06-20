package com.anantaya.backpackpro.upgrade.crafting;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class BackpackCraftingResultSlot extends ResultSlot {

    private final CraftingAccess craftingAccess;

    public BackpackCraftingResultSlot(
            Player player,
            CraftingContainer craftSlots,
            net.minecraft.world.Container resultSlots,
            int id,
            int x,
            int y,
            CraftingAccess craftingAccess
    ) {
        super(player, craftSlots, resultSlots, id, x, y);
        this.craftingAccess = craftingAccess;
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(@NonNull Player player) {
        return craftingAccess.hasCraftingUpgrade() && this.hasItem();
    }

    @Override
    public void onTake(@NonNull Player player, @NonNull ItemStack stack) {
        super.onTake(player, stack);

        craftingAccess.updateCraftingResult(player);
    }

    public interface CraftingAccess {
        boolean hasCraftingUpgrade();

        void updateCraftingResult(Player player);
    }
}