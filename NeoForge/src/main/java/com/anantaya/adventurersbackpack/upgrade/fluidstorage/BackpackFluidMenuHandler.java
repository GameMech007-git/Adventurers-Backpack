package com.anantaya.adventurersbackpack.upgrade.fluidstorage;

import com.anantaya.adventurersbackpack.backpack.BackpackInventory;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.block.entity.BackpackBlockEntity;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class BackpackFluidMenuHandler {

    private final AbstractContainerMenu menu;
    private final Container inventory;
    private final ItemStack backpackStack;
    private final @Nullable BackpackBlockEntity blockEntity;
    private final BackpackTier tier;

    public BackpackFluidMenuHandler(
            AbstractContainerMenu menu,
            Container inventory,
            ItemStack backpackStack,
            @Nullable BackpackBlockEntity blockEntity,
            BackpackTier tier
    ) {
        this.menu = menu;
        this.inventory = inventory;
        this.backpackStack = backpackStack;
        this.blockEntity = blockEntity;
        this.tier = tier;
    }

    public void handleFluidStorageClick(Player player) {
        if (player == null) {
            return;
        }

        if (!hasFluidTarget()) {
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

        saveFluidTarget();
        menu.broadcastChanges();
    }

    private boolean hasFluidTarget() {
        return !backpackStack.isEmpty() || blockEntity != null;
    }

    private FluidStorageType getStoredType() {
        if (blockEntity != null) {
            return blockEntity.getFluidType();
        }

        return BackpackFluidStorageHelper.getType(backpackStack);
    }

    private int getStoredAmount() {
        if (blockEntity != null) {
            return blockEntity.getFluidAmount();
        }

        return BackpackFluidStorageHelper.getAmount(backpackStack);
    }

    private boolean canInsertFluid(FluidStorageType insertedType) {
        if (insertedType == FluidStorageType.NONE) {
            return false;
        }

        int amount = getStoredAmount();

        if (amount >= BackpackFluidStorageHelper.capacityForTier(tier)) {
            return false;
        }

        FluidStorageType currentType = getStoredType();

        return amount <= 0
                || currentType == FluidStorageType.NONE
                || currentType == insertedType;
    }

    private boolean insertOneBucket(FluidStorageType insertedType) {
        if (!canInsertFluid(insertedType)) {
            return false;
        }

        int newAmount = getStoredAmount() + 1;

        if (blockEntity != null) {
            blockEntity.setFluidStorage(insertedType, newAmount);
            return true;
        }

        return BackpackFluidStorageHelper.insertOneBucket(
                backpackStack,
                insertedType,
                tier
        );
    }

    private boolean withdrawOneBucket(FluidStorageType requestedType) {
        if (requestedType == FluidStorageType.NONE) {
            return false;
        }

        if (getStoredAmount() <= 0) {
            return false;
        }

        if (getStoredType() != requestedType) {
            return false;
        }

        int newAmount = getStoredAmount() - 1;

        if (blockEntity != null) {
            if (newAmount <= 0) {
                blockEntity.setFluidStorage(FluidStorageType.NONE, 0);
            } else {
                blockEntity.setFluidStorage(requestedType, newAmount);
            }

            return true;
        }

        return BackpackFluidStorageHelper.withdrawOneBucket(
                backpackStack,
                requestedType,
                tier
        );
    }

    private void saveFluidTarget() {
        if (inventory instanceof BackpackInventory backpackInventory) {
            backpackInventory.saveToData();
        } else {
            inventory.setChanged();
        }

        if (blockEntity != null) {
            blockEntity.setChanged();
        }
    }

    private boolean tryInsertFluidBucket(
            Player player,
            FluidStorageType insertedType
    ) {
        if (!insertOneBucket(insertedType)) {
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

        FluidStorageType storedType = getStoredType();

        if (storedType == FluidStorageType.NONE) {
            return false;
        }

        ItemStack filledBucket = new ItemStack(storedType.filledBucketItem());

        if (carried.getCount() == 1) {
            if (!withdrawOneBucket(storedType)) {
                return false;
            }

            menu.setCarried(filledBucket);
            playFluidWithdrawSound(player, storedType);
            return true;
        }

        if (!player.getInventory().add(filledBucket.copy())) {
            return false;
        }

        if (!withdrawOneBucket(storedType)) {
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