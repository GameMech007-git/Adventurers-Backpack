package com.anantaya.adventurersbackpack.event;

import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BackpackDeathEventHandler {

    private static final Map<UUID, ItemStack> savedBackpacks = new HashMap<>();

    static {
        System.out.println("[Backpack] BackpackDeathEventHandler class loaded");
    }

    public static void register() {
        System.out.println("[Backpack] Registering death event handlers...");

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayer player) {
                saveBackpackBeforeDeath(player);
            }

            return true;
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            UUID playerId = oldPlayer.getUUID();
            ItemStack backpack = savedBackpacks.remove(playerId);

            if (backpack == null || backpack.isEmpty()) {
                System.out.println("[Backpack] No saved backpack for "
                        + newPlayer.getName().getString());
                return;
            }

            MinecraftServer server = newPlayer.level().getServer();
            HolderLookup.Provider registries = server.registryAccess();

            server.execute(() -> {
                ServerPlayer currentPlayer = server.getPlayerList().getPlayer(playerId);

                if (currentPlayer == null) {
                    System.out.println("[Backpack] Player " + playerId
                            + " not online at restore time — skipping.");
                    return;
                }

                if (!(backpack.getItem() instanceof BackpackItem backpackItem)) {
                    return;
                }

                int currentDur = BackpackItem.getDurability(backpack);
                int newDur = currentDur - 1;

                System.out.println("[Backpack] Durability for "
                        + currentPlayer.getName().getString()
                        + ": " + currentDur + " -> " + newDur);

                if (newDur <= 0) {
                    System.out.println("[Backpack] Durability reached 0 — destroying backpack");

                    dropAllMainInventorySlots(
                            currentPlayer,
                            backpack,
                            backpackItem.getTier(),
                            registries
                    );

                    dropAllExtraStorage(
                            currentPlayer,
                            backpack,
                            registries
                    );

                    currentPlayer.sendSystemMessage(
                            Component.literal("Your backpack has broken and disappeared!")
                                    .setStyle(Style.EMPTY.withColor(0xFF5555))
                    );

                    return;
                }

                BackpackItem.setDurability(backpack, newDur);

                if (newDur == 1) {
                    currentPlayer.sendSystemMessage(
                            Component.literal("Warning: Your backpack has only 1 durability left!")
                                    .setStyle(Style.EMPTY.withColor(0xFF5555))
                    );
                } else if (newDur <= backpackItem.getTier().maxDurability / 2) {
                    currentPlayer.sendSystemMessage(
                            Component.literal("Your backpack durability is low: " + newDur + " remaining.")
                                    .setStyle(Style.EMPTY.withColor(0xFFAA00))
                    );
                }

                System.out.println("[Backpack] Restoring backpack for "
                        + currentPlayer.getName().getString());

                boolean added = currentPlayer.addItem(backpack);

                System.out.println("[Backpack] addItem returned: " + added);

                if (added) {
                    currentPlayer.inventoryMenu.broadcastChanges();

                    System.out.println("[Backpack] ✓ Restored and synced for "
                            + currentPlayer.getName().getString());
                } else {
                    System.out.println("[Backpack] Inventory full — dropping at spawn");
                    currentPlayer.drop(backpack, false);
                }
            });
        });

        System.out.println("[Backpack] Death event handlers registered!");
    }

    private static void saveBackpackBeforeDeath(ServerPlayer player) {
        System.out.println("[Backpack] ALLOW_DEATH fired for " + player.getName().getString());

        HolderLookup.Provider registries = player.level().getServer().registryAccess();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.isEmpty() || !(stack.getItem() instanceof BackpackItem backpackItem)) {
                continue;
            }

            System.out.println("[Backpack] Found backpack at slot " + i);

            BackpackTier tier = backpackItem.getTier();

            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

            if (customData != null) {
                CompoundTag tag = customData.copyTag();

                /*
                 * Keep only protected slots.
                 *
                 * Protected slots:
                 * Slot 0 -> Slot protectedSlots - 1
                 *
                 * Everything after protected slots drops:
                 * normal storage + upgrade slots.
                 */
                dropAndRemoveUnprotectedMainSlots(
                        player,
                        tag,
                        tier,
                        registries
                );

                /*
                 * Optional penalty:
                 * Drop 1 random protected slot item on death.
                 */
                dropOneRandomProtectedSlot(
                        player,
                        tag,
                        tier,
                        registries
                );

                /*
                 * Extra storage does not survive death.
                 * It is saved as:
                 * ExtraStorage -> Slot0, Slot1, Slot2...
                 */
                dropAndRemoveExtraStorage(
                        player,
                        tag,
                        registries
                );

                /*
                 * Fluid storage does not survive death.
                 * Exact keys from BackpackFluidStorageHelper.
                 */
                tag.remove("BackpackProFluidType");
                tag.remove("BackpackProFluidAmount");

                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }

            ItemStack backpackCopy = stack.copy();

            savedBackpacks.put(player.getUUID(), backpackCopy);
            player.getInventory().setItem(i, ItemStack.EMPTY);

            System.out.println("[Backpack] ✓ Saved backpack for "
                    + player.getName().getString());

            break;
        }
    }

    private static void dropAndRemoveUnprotectedMainSlots(
            ServerPlayer player,
            CompoundTag tag,
            BackpackTier tier,
            HolderLookup.Provider registries
    ) {
        if (!tag.contains("Inventory")) {
            return;
        }

        CompoundTag invTag = tag.getCompound("Inventory").orElse(new CompoundTag());

        for (int s = tier.protectedSlots; s < tier.totalSlots; s++) {
            String key = "Slot" + s;

            if (!invTag.contains(key)) {
                continue;
            }

            ItemStack toDrop = readItemStack(invTag, key, registries);

            if (!toDrop.isEmpty()) {
                player.drop(toDrop, true);
                System.out.println("[Backpack] Dropped unprotected slot " + s + ": " + toDrop);
            }

            invTag.remove(key);
        }

        tag.put("Inventory", invTag);
    }

    private static void dropAndRemoveExtraStorage(
            ServerPlayer player,
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        if (!tag.contains("ExtraStorage")) {
            return;
        }

        CompoundTag extraTag = tag.getCompound("ExtraStorage").orElse(new CompoundTag());

        for (String key : extraTag.keySet()) {
            if (!key.startsWith("Slot")) {
                continue;
            }

            ItemStack toDrop = readItemStack(extraTag, key, registries);

            if (!toDrop.isEmpty()) {
                player.drop(toDrop, true);
                System.out.println("[Backpack] Dropped extra storage " + key + ": " + toDrop);
            }
        }

        tag.remove("ExtraStorage");
    }

    private static void dropAllMainInventorySlots(
            ServerPlayer player,
            ItemStack backpack,
            BackpackTier tier,
            HolderLookup.Provider registries
    ) {
        CustomData customData = backpack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return;
        }

        CompoundTag tag = customData.copyTag();

        if (!tag.contains("Inventory")) {
            return;
        }

        CompoundTag invTag = tag.getCompound("Inventory").orElse(new CompoundTag());

        for (int s = 0; s < tier.totalSlots; s++) {
            String key = "Slot" + s;

            if (!invTag.contains(key)) {
                continue;
            }

            ItemStack toDrop = readItemStack(invTag, key, registries);

            if (!toDrop.isEmpty()) {
                player.drop(toDrop, false);
                System.out.println("[Backpack] Broken backpack dropped slot " + s + ": " + toDrop);
            }
        }
    }

    private static void dropAllExtraStorage(
            ServerPlayer player,
            ItemStack backpack,
            HolderLookup.Provider registries
    ) {
        CustomData customData = backpack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return;
        }

        CompoundTag tag = customData.copyTag();

        if (!tag.contains("ExtraStorage")) {
            return;
        }

        CompoundTag extraTag = tag.getCompound("ExtraStorage").orElse(new CompoundTag());

        for (String key : extraTag.keySet()) {
            if (!key.startsWith("Slot")) {
                continue;
            }

            ItemStack toDrop = readItemStack(extraTag, key, registries);

            if (!toDrop.isEmpty()) {
                player.drop(toDrop, false);
                System.out.println("[Backpack] Broken backpack dropped extra storage " + key + ": " + toDrop);
            }
        }
    }

    private static ItemStack readItemStack(
            CompoundTag parentTag,
            String key,
            HolderLookup.Provider registries
    ) {
        Optional<CompoundTag> itemTagOptional = parentTag.getCompound(key);

        if (itemTagOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return ItemStack.CODEC
                .parse(
                        registries.createSerializationContext(NbtOps.INSTANCE),
                        itemTagOptional.get()
                )
                .result()
                .orElse(ItemStack.EMPTY);
    }

    private static void dropOneRandomProtectedSlot(
            ServerPlayer player,
            CompoundTag tag,
            BackpackTier tier,
            HolderLookup.Provider registries
    ) {
        if (!tag.contains("Inventory")) {
            return;
        }

        CompoundTag invTag = tag.getCompound("Inventory").orElse(new CompoundTag());

        java.util.List<String> occupiedProtectedSlots = new java.util.ArrayList<>();

        for (int s = 0; s < tier.protectedSlots; s++) {
            String key = "Slot" + s;

            if (invTag.contains(key)) {
                occupiedProtectedSlots.add(key);
            }
        }

        if (occupiedProtectedSlots.isEmpty()) {
            return;
        }

        String randomKey = occupiedProtectedSlots.get(
                player.getRandom().nextInt(occupiedProtectedSlots.size())
        );

        ItemStack toDrop = readItemStack(invTag, randomKey, registries);

        if (!toDrop.isEmpty()) {
            player.drop(toDrop, true);
            System.out.println("[Backpack] Dropped random protected item " + randomKey + ": " + toDrop);
        }

        invTag.remove(randomKey);
        tag.put("Inventory", invTag);
    }
}