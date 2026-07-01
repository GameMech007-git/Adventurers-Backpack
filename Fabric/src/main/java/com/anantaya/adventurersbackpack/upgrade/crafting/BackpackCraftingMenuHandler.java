package com.anantaya.adventurersbackpack.upgrade.crafting;

import com.anantaya.adventurersbackpack.backpack.BackpackScreenHandler;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;

public final class BackpackCraftingMenuHandler {

    private final BackpackScreenHandler menu;
    private final TransientCraftingContainer craftSlots;
    private final ResultContainer resultSlots;

    private final Container backpackInventory;
    private final BackpackTier tier;
    private int syncedCraftingActive = 0;

    public BackpackCraftingMenuHandler(
            BackpackScreenHandler menu,
            Container backpackInventory,
            BackpackTier tier
    ) {
        this.menu = menu;
        this.backpackInventory = backpackInventory;
        this.tier = tier;
        this.craftSlots = new TransientCraftingContainer(menu, 3, 3);
        this.resultSlots = new ResultContainer();
    }

    public TransientCraftingContainer craftSlots() {
        return craftSlots;
    }

    public ResultContainer resultSlots() {
        return resultSlots;
    }

    public boolean isCraftingContainer(Container container) {
        return container == craftSlots;
    }

    public boolean hasAnyCraftingInputItem() {
        for (int i = 0; i < craftSlots.getContainerSize(); i++) {
            if (!craftSlots.getItem(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public void addDataSlots() {
        menu.addMenuDataSlot(new DataSlot() {
            @Override
            public int get() {
                return hasUpgrade() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedCraftingActive = value;
            }
        });
    }

    public boolean hasUpgrade() {
        return BackpackUpgradeHelper.hasUpgrade(
                backpackInventory,
                BackpackUpgradeItem.Type.CRAFTING,
                tier
        );
    }

    public boolean isCraftingPanelHiddenSynced() {
        return syncedCraftingActive != 1;
    }

    public void slotsChanged(
            Container container,
            Player player
    ) {
        if (isCraftingContainer(container)) {
            updateCraftingResult(player);
        }
    }

    public void removed(Player player) {
        returnCraftingGridToPlayer(player);
    }

    public void updateCraftingResult(Player player) {
        if (player == null) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            resultSlots.setRecipeUsed(null);
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        CraftingInput input = craftSlots.asCraftInput();

        Optional<RecipeHolder<CraftingRecipe>> recipeOptional =
                serverLevel.recipeAccess().getRecipeFor(
                        RecipeType.CRAFTING,
                        input,
                        serverLevel
                );

        if (recipeOptional.isEmpty()) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            resultSlots.setRecipeUsed(null);
            menu.broadcastChanges();
            return;
        }

        RecipeHolder<CraftingRecipe> recipeHolder = recipeOptional.get();
        ItemStack result = recipeHolder.value().assemble(input);

        if (result.isEmpty()) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            resultSlots.setRecipeUsed(null);
            menu.broadcastChanges();
            return;
        }

        resultSlots.setRecipeUsed(recipeHolder);
        resultSlots.setItem(0, result);
        menu.broadcastChanges();
    }

    public void returnCraftingGridToPlayer(Player player) {
        if (player == null) {
            return;
        }

        for (int i = 0; i < craftSlots.getContainerSize(); i++) {
            ItemStack stack = craftSlots.removeItemNoUpdate(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }

        resultSlots.setItem(0, ItemStack.EMPTY);
        resultSlots.setRecipeUsed(null);
        menu.broadcastChanges();
    }
}