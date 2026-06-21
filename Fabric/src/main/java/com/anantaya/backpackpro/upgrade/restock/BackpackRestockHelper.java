package com.anantaya.backpackpro.upgrade.restock;

import com.anantaya.backpackpro.backpack.BackpackInventory;
import com.anantaya.backpackpro.backpack.BackpackItem;
import com.anantaya.backpackpro.backpack.BackpackTier;
import com.anantaya.backpackpro.upgrade.BackpackUpgradeFinder;
import com.anantaya.backpackpro.upgrade.BackpackUpgradeItem;
import com.anantaya.backpackpro.upgrade.config.RestockConfig;
import com.anantaya.backpackpro.upgrade.fluidstorage.BackpackFluidStorageHelper;
import com.anantaya.backpackpro.upgrade.fluidstorage.FluidStorageType;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BackpackRestockHelper {

    private static final int CHECK_INTERVAL_TICKS = 10;
    private static final int HOTBAR_SIZE = 9;
    private static final int FAILED_RESTOCK_COOLDOWN_TICKS = 40;

    private static final Map<UUID, PlayerRestockMemory> MEMORY = new HashMap<>();

    private BackpackRestockHelper() {
    }

    public static void tick(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        RegistryAccess registryAccess = player.level().registryAccess();

        ItemStack backpackStack = BackpackUpgradeFinder.findBackpackWithUpgrade(
                player,
                BackpackUpgradeItem.Type.RESTOCK,
                registryAccess
        );

        if (backpackStack.isEmpty()) {
            return;
        }

        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        BackpackTier tier = backpackItem.getTier();

        BackpackInventory backpackInventory = new BackpackInventory(
                backpackStack,
                tier.totalSlots,
                registryAccess
        );

        ItemStack restockUpgrade = BackpackUpgradeFinder.findUpgradeStack(
                backpackInventory,
                tier,
                BackpackUpgradeItem.Type.RESTOCK
        );

        if (restockUpgrade.isEmpty()) {
            return;
        }

        PlayerRestockMemory memory = MEMORY.computeIfAbsent(
                player.getUUID(),
                ignored -> new PlayerRestockMemory()
        );

        if (player.containerMenu != player.inventoryMenu) {
            memory.wasContainerOpen = true;
            return;
        }

        if (memory.wasContainerOpen) {
            memory.wasContainerOpen = false;
            reconcileHotbarAfterMenuClosed(player, memory);
            return;
        }

        boolean changed = tickSmartRestock(
                player,
                backpackStack,
                backpackInventory,
                tier,
                restockUpgrade,
                memory
        );

        if (changed) {
            backpackInventory.saveToData();
        }
    }

    private static boolean tickSmartRestock(
            ServerPlayer player,
            ItemStack backpackStack,
            BackpackInventory backpackInventory,
            BackpackTier tier,
            ItemStack restockUpgrade,
            PlayerRestockMemory memory
    ) {
        boolean changed = false;

        Inventory playerInventory = player.getInventory();

        for (int hotbarSlot = 0; hotbarSlot < HOTBAR_SIZE; hotbarSlot++) {
            ItemStack current = playerInventory.getItem(hotbarSlot);

            if (RestockConfig.shouldRestockBuckets(restockUpgrade)
                    && tryRestockFluidBucket(
                    player,
                    playerInventory,
                    hotbarSlot,
                    current,
                    backpackStack,
                    tier,
                    memory
            )) {
                changed = true;
                continue;
            }

            if (!current.isEmpty()) {
                if (tryRestockStackableItem(
                        player,
                        playerInventory,
                        hotbarSlot,
                        current,
                        backpackInventory,
                        tier,
                        memory
                )) {
                    changed = true;
                    current = playerInventory.getItem(hotbarSlot);
                }

                rememberHotbarSlot(memory, hotbarSlot, current);
                continue;
            }

            if (RestockConfig.shouldRestockTools(restockUpgrade)
                    && tryReplaceEmptyToolSlot(
                    player,
                    playerInventory,
                    hotbarSlot,
                    backpackInventory,
                    tier,
                    memory
            )) {
                changed = true;
            }
        }

        return changed;
    }

    private static boolean tryRestockStackableItem(
            ServerPlayer player,
            Inventory playerInventory,
            int hotbarSlot,
            ItemStack hotbarStack,
            BackpackInventory backpackInventory,
            BackpackTier tier,
            PlayerRestockMemory memory
    ) {
        if (hotbarStack.isEmpty()) {
            return false;
        }

        if (isIgnoredItem(hotbarStack)) {
            return false;
        }

        if (!hotbarStack.isStackable()) {
            return false;
        }

        int maxStackSize = hotbarStack.getMaxStackSize();

        if (maxStackSize <= 1) {
            return false;
        }

        int threshold = maxStackSize / 2;

        if (hotbarStack.getCount() > threshold) {
            return false;
        }

        HotbarSlotMemory slotMemory = memory.slots[hotbarSlot];

        if (isSlotOnCooldown(player, slotMemory)) {
            return false;
        }

        int needed = maxStackSize - hotbarStack.getCount();

        if (needed <= 0) {
            return false;
        }

        int moved = extractMatchingItemsFromNormalStorage(
                backpackInventory,
                tier,
                hotbarStack,
                needed
        );

        if (moved <= 0) {
            cooldownSlot(player, slotMemory);
            return false;
        }

        ItemStack updated = hotbarStack.copy();
        updated.grow(moved);

        playerInventory.setItem(hotbarSlot, updated);

        clearCooldown(slotMemory);
        playRestockSuccessSound(player);

        return true;
    }

    private static boolean tryReplaceEmptyToolSlot(
            ServerPlayer player,
            Inventory playerInventory,
            int hotbarSlot,
            BackpackInventory backpackInventory,
            BackpackTier tier,
            PlayerRestockMemory memory
    ) {
        HotbarSlotMemory slotMemory = memory.slots[hotbarSlot];

        if (isSlotOnCooldown(player, slotMemory)) {
            return false;
        }

        if (slotMemory.rememberedStack.isEmpty()) {
            return false;
        }

        if (!slotMemory.wasToolLike()) {
            return false;
        }

        ItemStack replacement = extractToolReplacementFromNormalStorage(
                backpackInventory,
                tier,
                slotMemory.rememberedStack
        );

        if (replacement.isEmpty()) {
            cooldownSlot(player, slotMemory);
            return false;
        }

        playerInventory.setItem(hotbarSlot, replacement);
        rememberHotbarSlot(memory, hotbarSlot, replacement);

        clearCooldown(slotMemory);
        playRestockSuccessSound(player);

        return true;
    }

    private static boolean tryRestockFluidBucket(
            ServerPlayer player,
            Inventory playerInventory,
            int hotbarSlot,
            ItemStack current,
            ItemStack backpackStack,
            BackpackTier tier,
            PlayerRestockMemory memory
    ) {
        if (current.isEmpty()) {
            return false;
        }

        if (!current.is(Items.BUCKET)) {
            return false;
        }

        HotbarSlotMemory slotMemory = memory.slots[hotbarSlot];

        if (isSlotOnCooldown(player, slotMemory)) {
            return false;
        }

        FluidStorageType rememberedFluid = slotMemory.rememberedFluidType;

        if (rememberedFluid == FluidStorageType.NONE) {
            return false;
        }

        if (!BackpackFluidStorageHelper.withdrawOneBucket(
                backpackStack,
                rememberedFluid,
                tier
        )) {
            cooldownSlot(player, slotMemory);
            return false;
        }

        ItemStack filledBucket = new ItemStack(rememberedFluid.filledBucketItem());

        playerInventory.setItem(hotbarSlot, filledBucket);
        rememberHotbarSlot(memory, hotbarSlot, filledBucket);

        clearCooldown(slotMemory);
        playRestockSuccessSound(player);

        return true;
    }

    private static int extractMatchingItemsFromNormalStorage(
            BackpackInventory backpackInventory,
            BackpackTier tier,
            ItemStack target,
            int maxAmount
    ) {
        int moved = 0;

        for (int i = 0; i < tier.normalSlots; i++) {
            if (moved >= maxAmount) {
                break;
            }

            int slotIndex = tier.normalStart() + i;
            ItemStack existing = backpackInventory.getItem(slotIndex);

            if (existing.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameComponents(existing, target)) {
                continue;
            }

            int amount = Math.min(maxAmount - moved, existing.getCount());

            ItemStack updated = existing.copy();
            updated.shrink(amount);

            if (updated.isEmpty()) {
                backpackInventory.setItem(slotIndex, ItemStack.EMPTY);
            } else {
                backpackInventory.setItem(slotIndex, updated);
            }

            moved += amount;
        }

        return moved;
    }

    private static ItemStack extractToolReplacementFromNormalStorage(
            BackpackInventory backpackInventory,
            BackpackTier tier,
            ItemStack remembered
    ) {
        ItemStack exact = extractFirstMatchingTool(
                backpackInventory,
                tier,
                remembered,
                true
        );

        if (!exact.isEmpty()) {
            return exact;
        }

        return extractFirstMatchingTool(
                backpackInventory,
                tier,
                remembered,
                false
        );
    }

    private static ItemStack extractFirstMatchingTool(
            BackpackInventory backpackInventory,
            BackpackTier tier,
            ItemStack remembered,
            boolean requireSameComponents
    ) {
        for (int i = 0; i < tier.normalSlots; i++) {
            int slotIndex = tier.normalStart() + i;
            ItemStack existing = backpackInventory.getItem(slotIndex);

            if (existing.isEmpty()) {
                continue;
            }

            if (!isToolLike(existing)) {
                continue;
            }

            boolean matches = requireSameComponents
                    ? ItemStack.isSameItemSameComponents(existing, remembered)
                    : ItemStack.isSameItem(existing, remembered);

            if (!matches) {
                continue;
            }

            ItemStack replacement = existing.copyWithCount(1);

            ItemStack updated = existing.copy();
            updated.shrink(1);

            if (updated.isEmpty()) {
                backpackInventory.setItem(slotIndex, ItemStack.EMPTY);
            } else {
                backpackInventory.setItem(slotIndex, updated);
            }

            return replacement;
        }

        return ItemStack.EMPTY;
    }

    private static void rememberHotbarSlot(
            PlayerRestockMemory memory,
            int hotbarSlot,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return;
        }

        if (isIgnoredItem(stack)) {
            return;
        }

        HotbarSlotMemory slotMemory = memory.slots[hotbarSlot];

        slotMemory.rememberedStack = stack.copy();
        slotMemory.rememberedFluidType = getFluidTypeFromBucket(stack);
    }

    private static FluidStorageType getFluidTypeFromBucket(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) {
            return FluidStorageType.WATER;
        }

        if (stack.is(Items.LAVA_BUCKET)) {
            return FluidStorageType.LAVA;
        }

        if (stack.is(Items.MILK_BUCKET)) {
            return FluidStorageType.MILK;
        }

        return FluidStorageType.NONE;
    }

    private static boolean isToolLike(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (isIgnoredItem(stack)) {
            return false;
        }

        return stack.getMaxStackSize() == 1
                && stack.getMaxDamage() > 0;
    }

    private static boolean isIgnoredItem(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem) {
            return true;
        }

        return stack.getItem() instanceof BackpackUpgradeItem;
    }

    private static final class PlayerRestockMemory {
        private final HotbarSlotMemory[] slots = new HotbarSlotMemory[HOTBAR_SIZE];
        private boolean wasContainerOpen = false;

        private PlayerRestockMemory() {
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                slots[i] = new HotbarSlotMemory();
            }
        }
    }

    private static void reconcileHotbarAfterMenuClosed(
            ServerPlayer player,
            PlayerRestockMemory memory
    ) {
        Inventory playerInventory = player.getInventory();

        for (int hotbarSlot = 0; hotbarSlot < HOTBAR_SIZE; hotbarSlot++) {
            ItemStack current = playerInventory.getItem(hotbarSlot);

            if (current.isEmpty() || isIgnoredItem(current)) {
                clearHotbarSlotMemory(memory, hotbarSlot);
                continue;
            }

            rememberHotbarSlot(memory, hotbarSlot, current);
        }
    }

    private static void clearHotbarSlotMemory(
            PlayerRestockMemory memory,
            int hotbarSlot
    ) {
        HotbarSlotMemory slotMemory = memory.slots[hotbarSlot];

        slotMemory.rememberedStack = ItemStack.EMPTY;
        slotMemory.rememberedFluidType = FluidStorageType.NONE;
        slotMemory.nextAllowedRestockTick = 0;
    }

    private static final class HotbarSlotMemory {
        private ItemStack rememberedStack = ItemStack.EMPTY;
        private FluidStorageType rememberedFluidType = FluidStorageType.NONE;
        private int nextAllowedRestockTick = 0;

        private boolean wasToolLike() {
            return isToolLike(rememberedStack);
        }
    }

    private static boolean isSlotOnCooldown(
            ServerPlayer player,
            HotbarSlotMemory slotMemory
    ) {
        return player.tickCount < slotMemory.nextAllowedRestockTick;
    }

    private static void cooldownSlot(
            ServerPlayer player,
            HotbarSlotMemory slotMemory
    ) {
        slotMemory.nextAllowedRestockTick =
                player.tickCount + FAILED_RESTOCK_COOLDOWN_TICKS;
    }

    private static void clearCooldown(HotbarSlotMemory slotMemory) {
        slotMemory.nextAllowedRestockTick = 0;
    }

    private static void playRestockSuccessSound(ServerPlayer player) {
        if (player.isSilent()) {
            return;
        }

        ServerLevel serverLevel = player.level();

        serverLevel.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BUBBLE_POP,
                SoundSource.PLAYERS,
                0.25F,
                1.35F + player.getRandom().nextFloat() * 0.15F
        );
    }
}