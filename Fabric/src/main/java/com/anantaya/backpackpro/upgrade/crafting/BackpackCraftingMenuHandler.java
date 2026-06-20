package com.anantaya.backpackpro.upgrade.crafting;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;

public final class BackpackCraftingMenuHandler {

    private final AbstractContainerMenu menu;
    private final TransientCraftingContainer craftSlots;
    private final ResultContainer resultSlots;

    public BackpackCraftingMenuHandler(AbstractContainerMenu menu) {
        this.menu = menu;
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