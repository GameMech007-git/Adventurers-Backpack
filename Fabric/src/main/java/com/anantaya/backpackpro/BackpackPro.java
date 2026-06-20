package com.anantaya.backpackpro;

import com.anantaya.backpackpro.event.BackpackDeathEventHandler;
import com.anantaya.backpackpro.event.BackpackServerTickHandler;
import com.anantaya.backpackpro.network.BackpackNetworking;
import com.anantaya.backpackpro.registry.ModBlockEntities;
import com.anantaya.backpackpro.registry.ModBlocks;
import com.anantaya.backpackpro.registry.ModCreativeTabs;
import com.anantaya.backpackpro.registry.ModItems;
import com.anantaya.backpackpro.registry.ModMenus;
import com.anantaya.backpackpro.registry.ModRecipeUnlocks;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackpackPro implements ModInitializer {

    public static final String MOD_ID = "backpack-pro";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModMenus.register();
        ModCreativeTabs.register();
        ModRecipeUnlocks.register();

        BackpackNetworking.register();
        BackpackDeathEventHandler.register();
        BackpackServerTickHandler.register();

        LOGGER.info("Backpack Pro loaded");
    }
}