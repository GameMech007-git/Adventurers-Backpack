package com.anantaya.adventurersbackpack;

import com.anantaya.adventurersbackpack.event.BackpackDeathEventHandler;
import com.anantaya.adventurersbackpack.event.BackpackServerTickHandler;
import com.anantaya.adventurersbackpack.network.BackpackNetworking;
import com.anantaya.adventurersbackpack.registry.ModBlockEntities;
import com.anantaya.adventurersbackpack.registry.ModBlocks;
import com.anantaya.adventurersbackpack.registry.ModCreativeTabs;
import com.anantaya.adventurersbackpack.registry.ModItems;
import com.anantaya.adventurersbackpack.registry.ModMenus;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AdventurersBackpack.MOD_ID)
public class AdventurersBackpack {

    public static final String MOD_ID = "adventurersbackpack";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public AdventurersBackpack(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        BackpackNetworking.register(modEventBus);

        BackpackDeathEventHandler.register();
        BackpackServerTickHandler.register();

        LOGGER.info("Adventurer's Backpack loaded");
    }
}