package com.anantaya.adventurersbackpack.event;

import com.anantaya.adventurersbackpack.upgrade.autopickup.BackpackAutoPickupHelper;
import com.anantaya.adventurersbackpack.upgrade.foodpouch.BackpackFoodPouchHelper;
import com.anantaya.adventurersbackpack.upgrade.recallrune.RecallRunePendingManager;
import com.anantaya.adventurersbackpack.upgrade.restock.BackpackRestockHelper;

import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class BackpackServerTickHandler {

    private BackpackServerTickHandler() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BackpackServerTickHandler::onServerTick);
    }

    private static void onServerTick(ServerTickEvent.Pre event) {
        RecallRunePendingManager.tick();

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            BackpackPlayerInventoryRules.enforceOneBackpack(player);

            BackpackAutoPickupHelper.tick(player);
            BackpackFoodPouchHelper.tick(player);
            BackpackRestockHelper.tick(player);
        }
    }
}