package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.menu.BackpackSlotRules;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseHelper;
import com.anantaya.adventurersbackpack.upgrade.crafting.BackpackCraftingResultSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class BackpackMenuSlotBuilder {

    private BackpackMenuSlotBuilder() {
    }

    public static void addSlots(
            BackpackScreenHandler menu,
            BackpackTier tier,
            BackpackMenuLayout layout,
            Container backpackInventory,
            Container extraStorageInventory,
            Container cartographersCaseInventory,
            Inventory playerInventory,
            ExtraStorageAccess extraStorageAccess,

            CraftingAccess craftingAccess
    ) {
        addMagicSlots(menu, tier, layout, backpackInventory);
        addNormalStorageSlots(menu, tier, layout, backpackInventory);

        addUpgradeSlots(
                menu,
                tier,
                layout,
                backpackInventory,
                extraStorageInventory,
                craftingAccess
        );

        addExtraStorageSlots(
                menu,
                tier,
                layout,
                extraStorageInventory,
                extraStorageAccess
        );

        addCraftingSlots(
                menu,
                layout,
                playerInventory,
                craftingAccess
        );

        addCartographersCaseSlots(
                menu,
                layout,
                cartographersCaseInventory
        );

        addPlayerInventorySlots(menu, layout, playerInventory);
        addHotbarSlots(menu, layout, playerInventory);
    }

    private static void addMagicSlots(
            BackpackScreenHandler menu,
            BackpackTier tier,
            BackpackMenuLayout layout,
            Container inventory
    ) {
        for (int i = 0; i < tier.protectedSlots; i++) {
            menu.addMenuSlot(new Slot(
                    inventory,
                    i,
                    layout.magicSlotX(i),
                    layout.magicSlotY(i)
            ) {
                @Override
                public boolean mayPlace(@NonNull ItemStack stack) {
                    return BackpackSlotRules.mayPlaceInMagicSlot(stack);
                }
            });
        }
    }

    private static void addNormalStorageSlots(
            BackpackScreenHandler menu,
            BackpackTier tier,
            BackpackMenuLayout layout,
            Container inventory
    ) {
        for (int i = 0; i < tier.normalSlots; i++) {
            final int slotIndex = tier.normalStart() + i;

            menu.addMenuSlot(new Slot(
                    inventory,
                    slotIndex,
                    layout.normalSlotX(i),
                    layout.normalSlotY(i)
            ) {
                @Override
                public boolean mayPlace(@NonNull ItemStack stack) {
                    return BackpackSlotRules.mayPlaceInNormalSlot(stack);
                }
            });
        }
    }

    private static void addUpgradeSlots(
            BackpackScreenHandler menu,
            BackpackTier tier,
            BackpackMenuLayout layout,
            Container inventory,
            Container extraStorageInventory,
            CraftingAccess craftingAccess
    ){
        for (int i = 0; i < tier.upgradeSlots; i++) {
            final int slotIndex = tier.upgradeStart() + i;

            menu.addMenuSlot(new Slot(
                    inventory,
                    slotIndex,
                    layout.upgradeSlotX(),
                    layout.upgradeSlotY(i)
            ) {
                @Override
                public boolean mayPlace(@NonNull ItemStack stack) {
                    return BackpackSlotRules.mayPlaceInUpgradeSlot(stack);
                }

                @Override
                public boolean mayPickup(@NonNull Player player) {
                    ItemStack stack = this.getItem();

                    if (BackpackUpgradeHelper.isUpgrade(
                            stack,
                            BackpackUpgradeItem.Type.CRAFTING
                    )) {
                        return !craftingAccess.hasAnyCraftingInputItem();
                    }

                    if (BackpackUpgradeHelper.isUpgrade(
                            stack,
                            BackpackUpgradeItem.Type.EXTRA_STORAGE
                    )) {
                        return !hasAnyItem(extraStorageInventory);
                    }

                    return true;
                }

                private static boolean hasAnyItem(Container container) {
                    if (container == null) {
                        return false;
                    }

                    for (int i = 0; i < container.getContainerSize(); i++) {
                        if (!container.getItem(i).isEmpty()) {
                            return true;
                        }
                    }

                    return false;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(@NonNull ItemStack stack) {
                    return 1;
                }
            });
        }
    }

    private static void addExtraStorageSlots(
            BackpackScreenHandler menu,
            BackpackTier tier,
            BackpackMenuLayout layout,
            Container extraStorageInventory,
            ExtraStorageAccess extraStorageAccess
    ) {
        int extraStorageSlots = BackpackMenuLayout.extraStorageSlotsForTier(tier);

        for (int i = 0; i < extraStorageSlots; i++) {
            menu.addMenuSlot(new Slot(
                    extraStorageInventory,
                    i,
                    layout.extraStorageSlotX(i),
                    layout.extraStorageSlotY(i)
            ) {
                @Override
                public boolean mayPlace(@NonNull ItemStack stack) {
                    return extraStorageAccess.canUseExtraStorageSlots()
                            && BackpackSlotRules.mayPlaceInNormalSlot(stack);
                }

                @Override
                public boolean mayPickup(@NonNull Player player) {
                    return extraStorageAccess.canUseExtraStorageSlots();
                }
            });
        }
    }

    private static void addCraftingSlots(
            BackpackScreenHandler menu,
            BackpackMenuLayout layout,
            Inventory playerInventory,
            CraftingAccess craftingAccess
    ) {
        int startX = layout.craftingGridX();
        int startY = layout.craftingGridY();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                final int slotIndex = col + row * 3;

                menu.addMenuSlot(new Slot(
                        craftingAccess.craftSlots(),
                        slotIndex,
                        startX + col * 18,
                        startY + row * 18
                ) {
                    @Override
                    public boolean mayPlace(@NonNull ItemStack stack) {
                        return craftingAccess.hasCraftingUpgrade();
                    }

                    @Override
                    public boolean mayPickup(@NonNull Player player) {
                        return craftingAccess.hasCraftingUpgrade();
                    }
                });
            }
        }

        menu.addMenuSlot(new BackpackCraftingResultSlot(
                playerInventory.player,
                craftingAccess.craftSlots(),
                craftingAccess.resultSlots(),
                0,
                layout.craftingResultX(),
                layout.craftingResultY(),
                new BackpackCraftingResultSlot.CraftingAccess() {
                    @Override
                    public boolean hasCraftingUpgrade() {
                        return craftingAccess.hasCraftingUpgrade();
                    }

                    @Override
                    public void updateCraftingResult(Player player) {
                        craftingAccess.updateCraftingResult(player);
                    }
                }
        ));
    }

    private static void addCartographersCaseSlots(
            BackpackScreenHandler menu,
            BackpackMenuLayout layout,
            Container cartographersCaseInventory
    ) {
        int panelX = layout.upgradeSlotX() + 31;
        int panelY = layout.upgradeSlotY(0);

        int gridX = 5;
        int gridY = 15;
        int slotSize = 18;

        for (int navigationSlot = 1; navigationSlot <= 8; navigationSlot++) {
            final int compassInventoryIndex =
                    CartographersCaseHelper.toCompassInventoryIndex(navigationSlot);

            int col = navigationSlot % 3;
            int row = navigationSlot / 3;

            int x = panelX + gridX + col * slotSize;
            int y = panelY + gridY + row * slotSize;

            menu.addMenuSlot(new Slot(
                    cartographersCaseInventory,
                    compassInventoryIndex,
                    x,
                    y
            ) {
                @Override
                public boolean mayPlace(@NonNull ItemStack stack) {
                    return menu.hasCartographersCaseUpgrade()
                            && CartographersCaseHelper.isValidLodestoneCompass(stack);
                }

                @Override
                public boolean mayPickup(@NonNull Player player) {
                    return menu.hasCartographersCaseUpgrade();
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(@NonNull ItemStack stack) {
                    return 1;
                }

                @Override
                public boolean isActive() {
                    return menu.isUpgradePanelOpen(BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE)
                            && menu.hasCartographersCaseUpgrade();
                }
            });
        }
    }

    private static void addPlayerInventorySlots(
            BackpackScreenHandler menu,
            BackpackMenuLayout layout,
            Inventory playerInventory
    ) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                menu.addMenuSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        layout.playerInvSlotX(col),
                        layout.playerInvSlotY(row)
                ));
            }
        }
    }

    private static void addHotbarSlots(
            BackpackScreenHandler menu,
            BackpackMenuLayout layout,
            Inventory playerInventory
    ) {
        for (int i = 0; i < 9; i++) {
            menu.addMenuSlot(new Slot(
                    playerInventory,
                    i,
                    layout.hotbarSlotX(i),
                    layout.hotbarY
            ));
        }
    }

    public interface ExtraStorageAccess {
        boolean canUseExtraStorageSlots();
    }
    public interface CraftingAccess {
        boolean hasCraftingUpgrade();

        boolean hasAnyCraftingInputItem();

        CraftingContainer craftSlots();

        ResultContainer resultSlots();

        void updateCraftingResult(Player player);
    }
}