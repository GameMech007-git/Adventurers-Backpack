package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.block.entity.BackpackBlockEntity;
import com.anantaya.adventurersbackpack.menu.BackpackSortHelper;
import com.anantaya.adventurersbackpack.menu.BackpackTrashHelper;
import com.anantaya.adventurersbackpack.registry.ModMenus;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigDispatcher;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.crafting.BackpackCraftingMenuHandler;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.BlockBackpackContainer;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.BlockExtraStorageContainer;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.ExtraStorageInventory;
import com.anantaya.adventurersbackpack.upgrade.fluidstorage.BackpackFluidMenuHandler;
import com.anantaya.adventurersbackpack.upgrade.fluidstorage.BackpackFluidStorageHelper;
import com.anantaya.adventurersbackpack.upgrade.fluidstorage.FluidStorageType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class BackpackScreenHandler extends AbstractContainerMenu {

    final Container inventory;
    final ItemStack backpackStack;
    final BackpackBlockEntity blockEntity;
    final BackpackMenuLayout layout;
    final Container extraStorageInventory;
    final int extraStorageSlots;
    final int sourceSlot;

    public final BackpackTier tier;

    private final BackpackMenuTransferHelper transferHelper;
    private final BackpackFluidMenuHandler fluidMenuHandler;
    private final BackpackCraftingMenuHandler craftingMenuHandler;
    private final Player menuPlayer;

    private int syncedFluidTypeId = FluidStorageType.NONE.networkId();
    private int syncedFluidAmount = 0;
    private int syncedExtraStorageActive = 0;
    private int syncedCraftingActive = 0;



    public BackpackScreenHandler(


            int syncId,
            Inventory playerInventory,
            ItemStack stack,
            BackpackTier tier,
            RegistryAccess registryAccess,
            int sourceSlot
    ) {
        super(menuTypeForTier(tier), syncId);

        System.out.println("[Backpack] ScreenHandler item constructor");
        System.out.println("[Backpack] syncId=" + syncId);
        System.out.println("[Backpack] tier=" + tier);
        System.out.println("[Backpack] sourceSlot=" + sourceSlot);
        System.out.println("[Backpack] stack=" + stack);
        System.out.println("[Backpack] stack item=" + stack.getItem());

        this.backpackStack = stack;
        this.blockEntity = null;
        this.sourceSlot = sourceSlot;
        this.tier = tier;
        this.layout = BackpackMenuLayout.of(tier);

        this.inventory = new BackpackInventory(
                stack,
                tier.totalSlots,
                registryAccess
        );

        this.extraStorageSlots = BackpackMenuLayout.extraStorageSlotsForTier(tier);

        this.extraStorageInventory = new ExtraStorageInventory(
                stack,
                extraStorageSlots,
                registryAccess
        );

        this.transferHelper = new BackpackMenuTransferHelper(
                this,
                tier,
                extraStorageInventory,
                extraStorageSlots,
                this::canUseExtraStorageSlots
        );

        this.fluidMenuHandler = new BackpackFluidMenuHandler(
                this,
                inventory,
                backpackStack,
                null,
                tier
        );

        this.craftingMenuHandler = new BackpackCraftingMenuHandler(this);
        this.menuPlayer = playerInventory.player;

        BackpackMenuSlotBuilder.addSlots(
                this,
                tier,
                layout,
                inventory,
                extraStorageInventory,
                playerInventory,
                this::canUseExtraStorageSlots,
                new BackpackMenuSlotBuilder.CraftingAccess() {
                    @Override
                    public boolean hasCraftingUpgrade() {
                        return BackpackScreenHandler.this.hasCraftingUpgrade();
                    }

                    @Override
                    public boolean hasAnyCraftingInputItem() {
                        return BackpackScreenHandler.this.hasAnyCraftingInputItem();
                    }

                    @Override
                    public CraftingContainer craftSlots() {
                        return BackpackScreenHandler.this.craftingMenuHandler.craftSlots();
                    }

                    @Override
                    public ResultContainer resultSlots() {
                        return BackpackScreenHandler.this.craftingMenuHandler.resultSlots();
                    }

                    @Override
                    public void updateCraftingResult(Player player) {
                        BackpackScreenHandler.this.craftingMenuHandler.updateCraftingResult(player);
                    }
                }
        );

        addFluidDataSlots();
        addExtraStorageDataSlots();
        addCraftingDataSlots();
    }

    public boolean isCraftingMenuSlot(int index) {
        return transferHelper.isCraftingSlot(index);
    }

    public boolean hasAnyCraftingInputItem() {
        return craftingMenuHandler != null
                && craftingMenuHandler.hasAnyCraftingInputItem();
    }

    public boolean hasCraftingUpgrade() {
        return BackpackUpgradeHelper.hasUpgrade(
                inventory,
                BackpackUpgradeItem.Type.CRAFTING,
                tier
        );
    }

    public boolean hasCraftingUpgradeSynced() {
        return syncedCraftingActive != 1;
    }

    public BackpackScreenHandler(
            int syncId,
            Inventory playerInventory,
            BackpackBlockEntity blockEntity
    ) {
        super(menuTypeForTier(blockEntity.getTier()), syncId);

        this.backpackStack = ItemStack.EMPTY;
        this.blockEntity = blockEntity;
        this.sourceSlot = -1;
        this.tier = blockEntity.getTier();
        this.layout = BackpackMenuLayout.of(this.tier);

        this.inventory = new BlockBackpackContainer(blockEntity);

        this.extraStorageSlots = BackpackMenuLayout.extraStorageSlotsForTier(this.tier);
        this.extraStorageInventory = createBlockExtraStorageContainer(blockEntity);

        this.transferHelper = new BackpackMenuTransferHelper(
                this,
                tier,
                extraStorageInventory,
                extraStorageSlots,
                this::canUseExtraStorageSlots
        );

        this.fluidMenuHandler = new BackpackFluidMenuHandler(
                this,
                inventory,
                backpackStack,
                blockEntity,
                tier
        );

        this.craftingMenuHandler = new BackpackCraftingMenuHandler(this);
        this.menuPlayer = playerInventory.player;

        BackpackMenuSlotBuilder.addSlots(
                this,
                tier,
                layout,
                inventory,
                extraStorageInventory,
                playerInventory,
                this::canUseExtraStorageSlots,
                new BackpackMenuSlotBuilder.CraftingAccess() {
                    @Override
                    public boolean hasCraftingUpgrade() {
                        return BackpackScreenHandler.this.hasCraftingUpgrade();
                    }

                    @Override
                    public boolean hasAnyCraftingInputItem() {
                        return BackpackScreenHandler.this.hasAnyCraftingInputItem();
                    }

                    @Override
                    public CraftingContainer craftSlots() {
                        return BackpackScreenHandler.this.craftingMenuHandler.craftSlots();
                    }

                    @Override
                    public ResultContainer resultSlots() {
                        return BackpackScreenHandler.this.craftingMenuHandler.resultSlots();
                    }

                    @Override
                    public void updateCraftingResult(Player player) {
                        BackpackScreenHandler.this.craftingMenuHandler.updateCraftingResult(player);
                    }
                }
        );

        addFluidDataSlots();
        addExtraStorageDataSlots();
        addCraftingDataSlots();
    }

    public BackpackScreenHandler(
            int syncId,
            Inventory playerInventory,
            ItemStack stack,
            BackpackTier tier,
            RegistryAccess registryAccess
    ) {
        this(
                syncId,
                playerInventory,
                stack,
                tier,
                registryAccess,
                findSourceSlot(playerInventory, stack)
        );
    }

    public FluidStorageType getSyncedFluidType() {
        return FluidStorageType.byNetworkId(syncedFluidTypeId);
    }

    public int getSyncedFluidAmount() {
        return syncedFluidAmount;
    }

    public boolean hasExtraStorageUpgradeSynced() {
        return syncedExtraStorageActive == 1;
    }

    public boolean canUseExtraStorageSlots() {
        if (BackpackUpgradeHelper.hasUpgrade(
                inventory,
                BackpackUpgradeItem.Type.EXTRA_STORAGE,
                tier
        )) {
            return true;
        }

        return hasExtraStorageUpgradeSynced();
    }

    public int extraStorageStart() {
        return transferHelper.extraStorageStart();
    }

    public int extraStorageEnd() {
        return transferHelper.extraStorageEnd();
    }

    public boolean isExtraStorageMenuSlot(int index) {
        return transferHelper.isExtraStorageMenuSlot(index);
    }

    public int playerMainInventoryStart() {
        return transferHelper.playerMainInventoryStart();
    }

    public int playerMainInventoryEnd() {
        return transferHelper.playerMainInventoryEnd();
    }

    public boolean moveStackToRange(
            ItemStack stack,
            int startIndex,
            int endIndex,
            boolean reverseDirection
    ) {
        return this.moveItemStackTo(
                stack,
                startIndex,
                endIndex,
                reverseDirection
        );
    }

    public void addMenuSlot(Slot slot) {
        this.addSlot(slot);
    }

    private void addFluidDataSlots() {
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (blockEntity != null) {
                    return blockEntity.getFluidType().networkId();
                }

                if (!backpackStack.isEmpty()) {
                    return BackpackFluidStorageHelper
                            .getType(backpackStack)
                            .networkId();
                }

                return FluidStorageType.NONE.networkId();
            }

            @Override
            public void set(int value) {
                syncedFluidTypeId = value;
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (blockEntity != null) {
                    return blockEntity.getFluidAmount();
                }

                if (!backpackStack.isEmpty()) {
                    return BackpackFluidStorageHelper.getAmount(backpackStack);
                }

                return 0;
            }

            @Override
            public void set(int value) {
                syncedFluidAmount = value;
            }
        });
    }

    private void addExtraStorageDataSlots() {
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return BackpackUpgradeHelper.hasUpgrade(
                        inventory,
                        BackpackUpgradeItem.Type.EXTRA_STORAGE,
                        tier
                ) ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedExtraStorageActive = value;
            }
        });
    }

    private void addCraftingDataSlots() {
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return BackpackUpgradeHelper.hasUpgrade(
                        inventory,
                        BackpackUpgradeItem.Type.CRAFTING,
                        tier
                ) ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedCraftingActive = value;
            }
        });
    }

    public void handleUpgradeConfigAction(
            int upgradeSlotIndex,
            BackpackUpgradeConfigAction action
    ) {
        BackpackUpgradeConfigDispatcher.apply(
                menuPlayer,
                inventory,
                extraStorageInventory,
                tier,
                upgradeSlotIndex,
                action
        );
    }

    public void handleFluidStorageClick(Player player) {
        fluidMenuHandler.handleFluidStorageClick(player);
    }

    public void handleTrashClick() {
        BackpackTrashHelper.trashCarriedStack(this);
    }

    public void handleSortNormalStorage(Player player) {
        if (player == null) {
            return;
        }

        boolean changed = BackpackSortHelper.sortAllSections(
                inventory,
                tier,
                extraStorageInventory,
                canUseExtraStorageSlots(),
                player.getInventory()
        );

        if (!changed) {
            return;
        }

        if (inventory instanceof BackpackInventory backpackInventory) {
            backpackInventory.saveToData();
        } else {
            inventory.setChanged();
        }

        if (extraStorageInventory != null) {
            extraStorageInventory.setChanged();
        }

        player.getInventory().setChanged();

        this.broadcastChanges();
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        Slot slot = this.slots.get(index);

        if (!slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();

        if (stackInSlot.getItem() instanceof BackpackItem) {
            return ItemStack.EMPTY;
        }

        ItemStack result = stackInSlot.copy();

        boolean moved;

        if (transferHelper.isBackpackMainSlot(index)) {
            moved = transferHelper.moveFromBackpackToPlayer(stackInSlot);
        } else if (transferHelper.isExtraStorageMenuSlot(index)) {
            moved = transferHelper.moveFromExtraStorageToPlayer(stackInSlot);
        } else if (transferHelper.isCraftingResultSlot(index)) {
            moved = this.moveStackToRange(
                    stackInSlot,
                    transferHelper.playerInventoryStart(),
                    this.slots.size(),
                    true
            );
        } else if (transferHelper.isCraftingInputSlot(index)) {
            moved = this.moveStackToRange(
                    stackInSlot,
                    transferHelper.playerInventoryStart(),
                    this.slots.size(),
                    false
            );
        } else {
            moved = transferHelper.moveFromPlayerToBackpack(stackInSlot);
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackInSlot);

        if (transferHelper.isCraftingResultSlot(index)) {
            craftingMenuHandler.updateCraftingResult(player);
        }

        return result;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        if (blockEntity != null) {
            return blockEntity.stillValidForPlayer(player);
        }

        if (sourceSlot >= 0
                && sourceSlot < player.getInventory().getContainerSize()) {
            ItemStack stack = player.getInventory().getItem(sourceSlot);

            return !stack.isEmpty()
                    && stack.getItem() instanceof BackpackItem
                    && ((BackpackItem) stack.getItem()).getTier() == tier;
        }

        return false;
    }

    @Override
    public void slotsChanged(@NonNull Container container) {
        super.slotsChanged(container);

        if (craftingMenuHandler != null
                && craftingMenuHandler.isCraftingContainer(container)) {
            craftingMenuHandler.updateCraftingResult(menuPlayer);
        }
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);

        if (inventory instanceof BackpackInventory backpackInventory) {
            backpackInventory.saveToData();

            if (extraStorageInventory != null) {
                extraStorageInventory.setChanged();
            }

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

            if (extraStorageInventory != null) {
                extraStorageInventory.setChanged();
            }

            if (blockEntity != null) {
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

        if (craftingMenuHandler != null) {
            craftingMenuHandler.returnCraftingGridToPlayer(player);
        }
    }

    private Container createBlockExtraStorageContainer(BackpackBlockEntity blockEntity) {
        if (blockEntity != null) {
            return new BlockExtraStorageContainer(blockEntity);
        }

        return new SimpleContainer(
                BackpackMenuLayout.extraStorageSlotsForTier(this.tier)
        );
    }

    private static int findSourceSlot(Inventory inventory, ItemStack targetStack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack == targetStack) {
                return i;
            }
        }

        return -1;
    }

    private static MenuType<BackpackScreenHandler> menuTypeForTier(BackpackTier tier) {
        return switch (tier) {
            case IRON -> ModMenus.BACKPACK_MENU_IRON.get();
            case DIAMOND -> ModMenus.BACKPACK_MENU_DIAMOND.get();
            case NETHERITE -> ModMenus.BACKPACK_MENU_NETHERITE.get();
        };
    }

    public boolean hasAnyExtraStorageItem() {
        if (extraStorageInventory == null) {
            return false;
        }

        for (int i = 0; i < extraStorageInventory.getContainerSize(); i++) {
            if (!extraStorageInventory.getItem(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }
}