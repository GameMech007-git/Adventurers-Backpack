package com.anantaya.adventurersbackpack;

import com.anantaya.adventurersbackpack.event.BackpackDeathEventHandler;
import com.anantaya.adventurersbackpack.event.BackpackServerTickHandler;
import com.anantaya.adventurersbackpack.network.BackpackNetworking;
import com.anantaya.adventurersbackpack.registry.ModBlockEntities;
import com.anantaya.adventurersbackpack.registry.ModBlocks;
import com.anantaya.adventurersbackpack.registry.ModCreativeTabs;
import com.anantaya.adventurersbackpack.registry.ModItems;
import com.anantaya.adventurersbackpack.registry.ModMenus;

import com.anantaya.adventurersbackpack.upgrade.recallrune.RecallRunePendingManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdventurersBackpack implements ModInitializer {

    public static final String MOD_ID = "adventurersbackpack";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModMenus.register();
        ModCreativeTabs.register();

        BackpackNetworking.register();
        BackpackDeathEventHandler.register();
        BackpackServerTickHandler.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            RecallRunePendingManager.tick();
        });

        LOGGER.info("Adventurer's Backpack loaded");
    }
}