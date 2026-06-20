package com.anantaya.backpackpro.block.entity;

import com.anantaya.backpackpro.backpack.BackpackInventory;
import com.anantaya.backpackpro.backpack.BackpackMenuLayout;
import com.anantaya.backpackpro.backpack.BackpackTier;
import com.anantaya.backpackpro.block.BackpackBlock;

import com.anantaya.backpackpro.registry.ModBlockEntities;
import com.anantaya.backpackpro.registry.ModItems;
import com.anantaya.backpackpro.upgrade.extrastorage.ExtraStorageInventory;
import com.anantaya.backpackpro.upgrade.fluidstorage.BackpackFluidStorageHelper;
import com.anantaya.backpackpro.upgrade.fluidstorage.FluidStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import static net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl.getMaxStackSize;

public class BackpackBlockEntity extends BlockEntity {

    private static final String EXTRA_STORAGE_KEY = "ExtraStorage";

    private final BackpackTier tier;
    private NonNullList<ItemStack> items;
    private NonNullList<ItemStack> extraStorageItems;
    private FluidStorageType fluidType = FluidStorageType.NONE;
    private int fluidAmount = 0;

    public BackpackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BACKPACK_BLOCK_ENTITY, pos, state);

        if (state.getBlock() instanceof BackpackBlock backpackBlock) {
            this.tier = backpackBlock.getTier();
        } else {
            this.tier = BackpackTier.IRON;
        }

        this.items = NonNullList.withSize(this.tier.totalSlots, ItemStack.EMPTY);
        this.extraStorageItems = NonNullList.withSize(
                BackpackMenuLayout.extraStorageSlotsForTier(this.tier),
                ItemStack.EMPTY
        );
    }

    public BackpackBlockEntity(BlockPos pos, BlockState state, BackpackTier tier) {
        super(ModBlockEntities.BACKPACK_BLOCK_ENTITY, pos, state);

        this.tier = tier;
        this.items = NonNullList.withSize(this.tier.totalSlots, ItemStack.EMPTY);
        this.extraStorageItems = NonNullList.withSize(
                BackpackMenuLayout.extraStorageSlotsForTier(this.tier),
                ItemStack.EMPTY
        );
    }

    public BackpackTier getTier() {
        return tier;
    }

    public int getExtraStorageSize() {
        return this.extraStorageItems.size();
    }

    public boolean isExtraStorageEmpty() {
        for (ItemStack stack : this.extraStorageItems) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public ItemStack getExtraStorageItem(int slot) {
        if (slot < 0 || slot >= this.extraStorageItems.size()) {
            return ItemStack.EMPTY;
        }

        return this.extraStorageItems.get(slot);
    }

    public ItemStack removeExtraStorageItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(
                this.extraStorageItems,
                slot,
                amount
        );

        if (!removed.isEmpty()) {
            setChanged();
        }

        return removed;
    }

    public ItemStack removeExtraStorageItemNoUpdate(int slot) {
        if (slot < 0 || slot >= this.extraStorageItems.size()) {
            return ItemStack.EMPTY;
        }

        return ContainerHelper.takeItem(this.extraStorageItems, slot);
    }

    public void setExtraStorageItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.extraStorageItems.size()) {
            return;
        }

        this.extraStorageItems.set(slot, stack);

        if (!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()) {
            stack.setCount(stack.getMaxStackSize());
        }

        setChanged();
    }

    public ItemStack createDroppedBackpackStack(RegistryAccess registryAccess) {
        ItemStack drop = switch (this.tier) {
            case IRON -> new ItemStack(ModItems.BACKPACK_IRON);
            case DIAMOND -> new ItemStack(ModItems.BACKPACK_DIAMOND);
            case NETHERITE -> new ItemStack(ModItems.BACKPACK_NETHERITE);
        };

        BackpackInventory itemInventory = new BackpackInventory(
                drop,
                this.tier.totalSlots,
                registryAccess
        );

        for (int i = 0; i < this.tier.totalSlots; i++) {
            itemInventory.setItem(i, this.getItem(i).copy());
        }

        itemInventory.saveToData();

        ExtraStorageInventory itemExtraStorage = new ExtraStorageInventory(
                drop,
                BackpackMenuLayout.extraStorageSlotsForTier(this.tier),
                registryAccess
        );

        for (int i = 0; i < this.extraStorageItems.size(); i++) {
            itemExtraStorage.setItem(i, this.extraStorageItems.get(i).copy());
        }

        itemExtraStorage.saveToData();

        BackpackFluidStorageHelper.setFluid(
                drop,
                this.fluidType,
                this.fluidAmount
        );

        return drop;
    }

    public void loadFromBackpackStack(
            ItemStack stack,
            RegistryAccess registryAccess
    ) {
        BackpackInventory itemInventory = new BackpackInventory(
                stack,
                this.tier.totalSlots,
                registryAccess
        );

        for (int i = 0; i < this.tier.totalSlots; i++) {
            this.setItem(i, itemInventory.getItem(i).copy());
        }

        ExtraStorageInventory itemExtraStorage = new ExtraStorageInventory(
                stack,
                BackpackMenuLayout.extraStorageSlotsForTier(this.tier),
                registryAccess
        );

        for (int i = 0; i < this.extraStorageItems.size(); i++) {
            this.extraStorageItems.set(i, itemExtraStorage.getItem(i).copy());
        }

        this.fluidType = BackpackFluidStorageHelper.getType(stack);
        this.fluidAmount = BackpackFluidStorageHelper.getAmount(stack);

        this.setChanged();
    }


    public int getContainerSize() {
        return this.items.size();
    }

    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }


    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= this.items.size()) {
            return ItemStack.EMPTY;
        }

        return this.items.get(slot);
    }


    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(this.items, slot, amount);

        if (!removed.isEmpty()) {
            setChanged();
        }

        return removed;
    }


    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= this.items.size()) {
            return ItemStack.EMPTY;
        }

        return ContainerHelper.takeItem(this.items, slot);
    }


    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.items.size()) {
            return;
        }

        this.items.set(slot, stack);

        if (!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()) {
            stack.setCount(stack.getMaxStackSize());
        }

        setChanged();
    }


    public boolean stillValidForPlayer(Player player) {
        if (this.level == null) {
            return false;
        }

        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }

        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    public void clearContent() {
        this.items.replaceAll(stack -> ItemStack.EMPTY);
        this.extraStorageItems.replaceAll(stack -> ItemStack.EMPTY);
        setChanged();
    }

    public FluidStorageType getFluidType() {
        return this.fluidType;
    }

    public int getFluidAmount() {
        return this.fluidAmount;
    }

    public void setFluidStorage(FluidStorageType type, int amount) {
        this.fluidType = type == null ? FluidStorageType.NONE : type;
        this.fluidAmount = Math.max(0, Math.min(
                BackpackFluidStorageHelper.CAPACITY_BUCKETS,
                amount
        ));

        if (this.fluidAmount <= 0) {
            this.fluidType = FluidStorageType.NONE;
            this.fluidAmount = 0;
        }

        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, this.items);

        ValueOutput extraOutput = output.child(EXTRA_STORAGE_KEY);
        ContainerHelper.saveAllItems(extraOutput, this.extraStorageItems);

        output.putString("FluidType", this.fluidType.id());
        output.putInt("FluidAmount", this.fluidAmount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.items = NonNullList.withSize(this.tier.totalSlots, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.extraStorageItems = NonNullList.withSize(
                BackpackMenuLayout.extraStorageSlotsForTier(this.tier),
                ItemStack.EMPTY
        );

        input.child(EXTRA_STORAGE_KEY).ifPresent(extraInput ->
                ContainerHelper.loadAllItems(extraInput, this.extraStorageItems)
        );

        this.fluidType = FluidStorageType.byId(
                input.getString("FluidType").orElse(FluidStorageType.NONE.id())
        );

        this.fluidAmount = Math.max(0, Math.min(
                BackpackFluidStorageHelper.CAPACITY_BUCKETS,
                input.getInt("FluidAmount").orElse(0)
        ));

        if (this.fluidAmount <= 0) {
            this.fluidType = FluidStorageType.NONE;
        }
    }

}