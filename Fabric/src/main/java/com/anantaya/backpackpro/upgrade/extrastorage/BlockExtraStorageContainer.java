package com.anantaya.backpackpro.upgrade.extrastorage;

import com.anantaya.backpackpro.block.entity.BackpackBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BlockExtraStorageContainer implements Container {

    private final BackpackBlockEntity blockEntity;

    public BlockExtraStorageContainer(BackpackBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getContainerSize() {
        return blockEntity == null ? 0 : blockEntity.getExtraStorageSize();
    }

    @Override
    public boolean isEmpty() {
        return blockEntity == null || blockEntity.isExtraStorageEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (blockEntity == null) {
            return ItemStack.EMPTY;
        }

        return blockEntity.getExtraStorageItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (blockEntity == null) {
            return ItemStack.EMPTY;
        }

        return blockEntity.removeExtraStorageItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (blockEntity == null) {
            return ItemStack.EMPTY;
        }

        return blockEntity.removeExtraStorageItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (blockEntity == null) {
            return;
        }

        blockEntity.setExtraStorageItem(slot, stack);
    }

    @Override
    public void setChanged() {
        if (blockEntity != null) {
            blockEntity.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && blockEntity.stillValidForPlayer(player);
    }

    @Override
    public void clearContent() {
        if (blockEntity == null) {
            return;
        }

        for (int i = 0; i < getContainerSize(); i++) {
            setItem(i, ItemStack.EMPTY);
        }

        setChanged();
    }
}