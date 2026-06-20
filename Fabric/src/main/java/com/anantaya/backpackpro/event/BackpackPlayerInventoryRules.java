package com.anantaya.backpackpro.event;

import com.anantaya.backpackpro.backpack.BackpackItem;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BackpackPlayerInventoryRules {

    private BackpackPlayerInventoryRules() {
    }

    public static void enforceOneBackpack(ServerPlayer player) {
        List<Integer> backpackSlots = findBackpackSlots(player);

        if (backpackSlots.size() <= 1) {
            return;
        }

        dropExtraBackpacks(player, backpackSlots);

        player.sendSystemMessage(
                Component.literal("Only one backpack can be equipped at a time!")
                        .setStyle(Style.EMPTY.withColor(0xFF5555))
        );
    }

    private static List<Integer> findBackpackSlots(ServerPlayer player) {
        List<Integer> backpackSlots = new ArrayList<>();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.getItem() instanceof BackpackItem) {
                backpackSlots.add(i);
            }
        }

        return backpackSlots;
    }

    private static void dropExtraBackpacks(
            ServerPlayer player,
            List<Integer> backpackSlots
    ) {
        for (int i = 1; i < backpackSlots.size(); i++) {
            int slot = backpackSlots.get(i);

            ItemStack toDrop = player.getInventory().getItem(slot).copy();

            player.getInventory().setItem(slot, ItemStack.EMPTY);
            player.drop(toDrop, false);
        }
    }
}