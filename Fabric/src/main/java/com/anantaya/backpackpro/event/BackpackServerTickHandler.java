package com.anantaya.backpackpro.event;

import com.anantaya.backpackpro.upgrade.autopickup.BackpackAutoPickupHelper;
import com.anantaya.backpackpro.upgrade.foodpouch.BackpackFoodPouchHelper;

import com.anantaya.backpackpro.upgrade.restock.BackpackRestockHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.level.ServerPlayer;

public final class BackpackServerTickHandler {

    private BackpackServerTickHandler() {
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                BackpackPlayerInventoryRules.enforceOneBackpack(player);

                BackpackAutoPickupHelper.tick(player);
                BackpackFoodPouchHelper.tick(player);
                BackpackRestockHelper.tick(player);
            }
        });
    }
}