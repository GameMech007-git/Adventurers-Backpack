package com.anantaya.backpackpro.upgrade.extrastorage;

import com.anantaya.backpackpro.block.entity.BackpackBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BlockBackpackContainer implements Container {

    private final BackpackBlockEntity blockEntity;

    public BlockBackpackContainer(BackpackBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getContainerSize() {
        return blockEntity.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return blockEntity.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return blockEntity.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return blockEntity.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return blockEntity.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        blockEntity.setItem(slot, stack);
    }

    @Override
    public void setChanged() {
        blockEntity.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValidForPlayer(player);
    }

    @Override
    public void clearContent() {
        blockEntity.clearContent();
    }
}