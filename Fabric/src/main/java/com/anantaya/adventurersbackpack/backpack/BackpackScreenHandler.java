package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.block.entity.BackpackBlockEntity;
import com.anantaya.adventurersbackpack.menu.BackpackTrashHelper;
import com.anantaya.adventurersbackpack.registry.ModMenus;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigDispatcher;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseHelper;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseInventory;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseMenuHandler;
import com.anantaya.adventurersbackpack.upgrade.crafting.BackpackCraftingMenuHandler;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.BlockBackpackContainer;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.BlockExtraStorageContainer;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.ExtraStorageInventory;
import com.anantaya.adventurersbackpack.upgrade.extrastorage.ExtraStorageMenuHandler;
import com.anantaya.adventurersbackpack.upgrade.fluidstorage.BackpackFluidMenuHandler;
import com.anantaya.adventurersbackpack.upgrade.fluidstorage.FluidStorageType;
import com.anantaya.adventurersbackpack.upgrade.nested.NestedUpgradeData;
import com.anantaya.adventurersbackpack.upgrade.nested.NestedUpgradeInventory;
import com.anantaya.adventurersbackpack.upgrade.nested.NestedUpgradeMenuHandler;
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
    final Container cartographersCaseInventory;
    final Container nestedUpgradeInventory;
    final int extraStorageSlots;
    final int sourceSlot;


    public final BackpackTier tier;

    private final BackpackMenuTransferHelper transferHelper;
    private final BackpackFluidMenuHandler fluidMenuHandler;
    private final BackpackCraftingMenuHandler craftingMenuHandler;
    private final Player menuPlayer;
    private final UpgradePanelState upgradePanelState;
    private final CartographersCaseMenuHandler cartographersCaseMenuHandler;
    private final NestedUpgradeMenuHandler nestedUpgradeMenuHandler;
    private final ExtraStorageMenuHandler extraStorageMenuHandler;

    public BackpackScreenHandler(
            int syncId,
            Inventory playerInventory,
            ItemStack stack,
            BackpackTier tier,
            RegistryAccess registryAccess,
            int sourceSlot
    ) {
        super(menuTypeForTier(tier), syncId);

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

        this.extraStorageMenuHandler = new ExtraStorageMenuHandler(
                this,
                inventory,
                extraStorageInventory,
                tier
        );

        this.cartographersCaseInventory = new CartographersCaseInventory(
                stack,
                registryAccess
        );

        this.nestedUpgradeInventory = new NestedUpgradeInventory(
                stack,
                registryAccess
        );

        this.upgradePanelState = new UpgradePanelState();

        this.cartographersCaseMenuHandler = new CartographersCaseMenuHandler(
                this,
                inventory,
                cartographersCaseInventory,
                backpackStack,
                tier
        );

        this.nestedUpgradeMenuHandler = new NestedUpgradeMenuHandler(
                this,
                inventory,
                nestedUpgradeInventory,
                backpackStack,
                tier
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

        this.craftingMenuHandler = new BackpackCraftingMenuHandler(
                this,
                inventory,
                tier
        );
        this.menuPlayer = playerInventory.player;

        addBackpackSlots(playerInventory);

        addUpgradeDataSlots();
    }

    public boolean isUpgradePanelOpen(BackpackUpgradeItem.Type type) {
        return upgradePanelState.isOpen(type, this.slots);
    }

    public void setOpenUpgradePanel(
            BackpackUpgradeItem.Type type,
            int upgradeSlotIndex
    ) {
        upgradePanelState.open(type, upgradeSlotIndex);
    }

    public void closeUpgradePanel() {
        upgradePanelState.close();
    }

    public boolean isCraftingMenuSlot(int index) {
        return transferHelper.isCraftingSlot(index);
    }

    public boolean hasAnyCraftingInputItem() {
        return craftingMenuHandler.hasAnyCraftingInputItem();
    }

    public boolean hasCraftingUpgrade() {
        return craftingMenuHandler.hasUpgrade();
    }

    public boolean isCraftingPanelHiddenSynced() {
        return craftingMenuHandler.isCraftingPanelHiddenSynced();
    }

    public ItemStack getBackpackStackForClient() {
        return backpackStack;
    }
    public BackpackScreenHandler(
            int syncId,
            Inventory playerInventory,
            BackpackBlockEntity blockEntity
    ) {
        super(menuTypeForTier(blockEntity.getTier()), syncId);

        this.backpackStack = blockEntity.createCartographersCaseBackpackStack();
        this.blockEntity = blockEntity;
        this.sourceSlot = -1;
        this.tier = blockEntity.getTier();
        this.layout = BackpackMenuLayout.of(this.tier);

        this.inventory = new BlockBackpackContainer(blockEntity);

        this.extraStorageSlots = BackpackMenuLayout.extraStorageSlotsForTier(this.tier);
        this.extraStorageInventory = createBlockExtraStorageContainer(blockEntity);

        this.extraStorageMenuHandler = new ExtraStorageMenuHandler(
                this,
                inventory,
                extraStorageInventory,
                tier
        );

        this.cartographersCaseInventory = new CartographersCaseInventory(
                backpackStack,
                playerInventory.player.level().registryAccess()
        );

        this.nestedUpgradeInventory = new NestedUpgradeInventory(
                backpackStack,
                playerInventory.player.level().registryAccess()
        );

        this.upgradePanelState = new UpgradePanelState();

        this.cartographersCaseMenuHandler = new CartographersCaseMenuHandler(
                this,
                inventory,
                cartographersCaseInventory,
                backpackStack,
                tier
        );

        this.nestedUpgradeMenuHandler = new NestedUpgradeMenuHandler(
                this,
                inventory,
                nestedUpgradeInventory,
                backpackStack,
                tier
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
                blockEntity,
                tier
        );

        this.craftingMenuHandler = new BackpackCraftingMenuHandler(
                this,
                inventory,
                tier
        );
        this.menuPlayer = playerInventory.player;

        addBackpackSlots(playerInventory);

        addUpgradeDataSlots();
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
        return fluidMenuHandler.getSyncedFluidType();
    }

    public int getSyncedFluidAmount() {
        return fluidMenuHandler.getSyncedFluidAmount();
    }

    public boolean hasExtraStorageUpgradeSynced() {
        return extraStorageMenuHandler.hasUpgradeSynced();
    }

    public boolean canUseExtraStorageSlots() {
        return extraStorageMenuHandler.canUseSlots();
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

    private void addUpgradeDataSlots() {
        fluidMenuHandler.addDataSlots();
        extraStorageMenuHandler.addDataSlots();
        craftingMenuHandler.addDataSlots();
        cartographersCaseMenuHandler.addDataSlots();
    }

    public void addMenuSlot(Slot slot) {
        this.addSlot(slot);
    }

    public void addMenuDataSlot(DataSlot dataSlot) {
        this.addDataSlot(dataSlot);
    }

    private void addBackpackSlots(Inventory playerInventory) {
        BackpackMenuSlotBuilder.addSlots(
                this,
                tier,
                layout,
                inventory,
                extraStorageInventory,
                cartographersCaseInventory,
                nestedUpgradeInventory,
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
        boolean changed = BackpackMenuSortHandler.sortNormalStorage(
                player,
                inventory,
                tier,
                extraStorageInventory,
                canUseExtraStorageSlots(),
                extraStorageMenuHandler
        );

        if (changed) {
            this.broadcastChanges();
        }
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        return BackpackMenuQuickMoveHandler.quickMoveStack(
                this,
                transferHelper,
                craftingMenuHandler,
                player,
                index
        );
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
        craftingMenuHandler.slotsChanged(container, menuPlayer);
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);

        BackpackMenuSaveHandler.removed(
                player,
                inventory,
                backpackStack,
                blockEntity,
                tier,
                extraStorageMenuHandler,
                cartographersCaseMenuHandler,
                nestedUpgradeMenuHandler,
                craftingMenuHandler
        );
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
            case IRON -> ModMenus.BACKPACK_MENU_IRON;
            case DIAMOND -> ModMenus.BACKPACK_MENU_DIAMOND;
            case NETHERITE -> ModMenus.BACKPACK_MENU_NETHERITE;
        };
    }

    public boolean hasAnyExtraStorageItem() {
        return extraStorageMenuHandler.hasAnyStoredItem();
    }

    public int getSyncedCartographerActiveSlot() {
        return cartographersCaseMenuHandler.getSyncedActiveSlot();
    }

    public boolean hasCartographersCaseUpgrade() {
        return cartographersCaseMenuHandler.hasUpgrade();
    }

    public boolean hasAnyCartographersCaseItem() {
        return cartographersCaseMenuHandler.hasAnyStoredItem();
    }

    public boolean hasNestedUpgrade() {
        return nestedUpgradeMenuHandler.hasUpgrade();
    }

    public boolean hasAnyNestedUpgradeItem() {
        return nestedUpgradeMenuHandler.hasAnyStoredItem();
    }

    public void handleCartographersCaseClick(
            Player player,
            int upgradeSlotIndex,
            int navigationSlot
    ) {
        cartographersCaseMenuHandler.handleClick(
                player,
                upgradeSlotIndex,
                navigationSlot
        );
    }

    public boolean hasUpgradeInstalled(BackpackUpgradeItem.Type type) {
        if (BackpackUpgradeHelper.hasUpgrade(
                inventory,
                type,
                tier
        )) {
            return true;
        }

        return NestedUpgradeData.hasUpgrade(
                nestedUpgradeInventory,
                type
        );
    }

    public boolean hasFluidStorageUpgrade() {
        return hasUpgradeInstalled(BackpackUpgradeItem.Type.FLUID_STORAGE);
    }
}