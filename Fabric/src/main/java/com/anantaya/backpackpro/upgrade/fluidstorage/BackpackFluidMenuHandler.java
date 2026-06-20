package com.anantaya.backpackpro.upgrade.fluidstorage;

import com.anantaya.backpackpro.backpack.BackpackInventory;
import com.anantaya.backpackpro.backpack.BackpackTier;
import com.anantaya.backpackpro.upgrade.BackpackUpgradeHelper;
import com.anantaya.backpackpro.upgrade.BackpackUpgradeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BackpackFluidMenuHandler {

    private final AbstractContainerMenu menu;
    private final Container inventory;
    private final ItemStack backpackStack;
    private final BackpackTier tier;

    public BackpackFluidMenuHandler(
            AbstractContainerMenu menu,
            Container inventory,
            ItemStack backpackStack,
            BackpackTier tier
    ) {
        this.menu = menu;
        this.inventory = inventory;
        this.backpackStack = backpackStack;
        this.tier = tier;
    }

    public void handleFluidStorageClick(Player player) {
        if (player == null) {
            return;
        }

        if (backpackStack.isEmpty()) {
            return;
        }

        boolean hasFluidStorage = BackpackUpgradeHelper.hasUpgrade(
                inventory,
                BackpackUpgradeItem.Type.FLUID_STORAGE,
                tier
        );

        if (!hasFluidStorage) {
            return;
        }

        ItemStack carried = menu.getCarried();

        if (carried.isEmpty()) {
            return;
        }

        boolean changed = false;

        if (carried.is(Items.WATER_BUCKET)) {
            if (carried.getCount() != 1) {
                return;
            }

            changed = tryInsertFluidBucket(player, FluidStorageType.WATER);
        } else if (carried.is(Items.LAVA_BUCKET)) {
            if (carried.getCount() != 1) {
                return;
            }

            changed = tryInsertFluidBucket(player, FluidStorageType.LAVA);
        } else if (carried.is(Items.MILK_BUCKET)) {
            if (carried.getCount() != 1) {
                return;
            }

            changed = tryInsertFluidBucket(player, FluidStorageType.MILK);
        } else if (carried.is(Items.BUCKET)) {
            changed = tryWithdrawFluidBucket(player);
        }

        if (!changed) {
            playFluidInvalidSound(player);
            return;
        }

        if (inventory instanceof BackpackInventory backpackInventory) {
            backpackInventory.saveToData();
        } else {
            inventory.setChanged();
        }

        menu.broadcastChanges();
    }

    private boolean tryInsertFluidBucket(
            Player player,
            FluidStorageType insertedType
    ) {
        if (!BackpackFluidStorageHelper.insertOneBucket(
                backpackStack,
                insertedType
        )) {
            return false;
        }

        menu.setCarried(new ItemStack(Items.BUCKET));
        playFluidInsertSound(player, insertedType);

        return true;
    }

    private boolean tryWithdrawFluidBucket(Player player) {
        ItemStack carried = menu.getCarried();

        if (!carried.is(Items.BUCKET)) {
            return false;
        }

        FluidStorageType storedType = BackpackFluidStorageHelper.getType(backpackStack);

        if (storedType == FluidStorageType.NONE) {
            return false;
        }

        ItemStack filledBucket = new ItemStack(storedType.filledBucketItem());

        if (carried.getCount() == 1) {
            if (!BackpackFluidStorageHelper.withdrawOneBucket(backpackStack, storedType)) {
                return false;
            }

            menu.setCarried(filledBucket);
            playFluidWithdrawSound(player, storedType);
            return true;
        }

        if (!player.getInventory().add(filledBucket.copy())) {
            return false;
        }

        if (!BackpackFluidStorageHelper.withdrawOneBucket(backpackStack, storedType)) {
            return false;
        }

        ItemStack remainingBuckets = carried.copy();
        remainingBuckets.shrink(1);

        menu.setCarried(remainingBuckets);
        playFluidWithdrawSound(player, storedType);
        return true;
    }

    private void playFluidInsertSound(
            Player player,
            FluidStorageType type
    ) {
        SoundEvent sound = switch (type) {
            case WATER, MILK -> SoundEvents.BUCKET_EMPTY;
            case LAVA -> SoundEvents.BUCKET_EMPTY_LAVA;
            case NONE -> null;
        };

        playFluidSound(
                player,
                sound,
                0.75F,
                0.95F + player.getRandom().nextFloat() * 0.1F
        );
    }

    private void playFluidWithdrawSound(
            Player player,
            FluidStorageType type
    ) {
        SoundEvent sound = switch (type) {
            case WATER, MILK -> SoundEvents.BUCKET_FILL;
            case LAVA -> SoundEvents.BUCKET_FILL_LAVA;
            case NONE -> null;
        };

        playFluidSound(
                player,
                sound,
                0.75F,
                0.95F + player.getRandom().nextFloat() * 0.1F
        );
    }

    private void playFluidInvalidSound(Player player) {
        playFluidSound(
                player,
                SoundEvents.BUBBLE_POP,
                0.35F,
                0.75F + player.getRandom().nextFloat() * 0.1F
        );
    }

    private void playFluidSound(
            Player player,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        if (sound == null) {
            return;
        }

        if (player.isSilent()) {
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundSource.PLAYERS,
                volume,
                pitch
        );
    }
}