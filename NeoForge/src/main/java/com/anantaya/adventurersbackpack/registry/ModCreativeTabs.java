package com.anantaya.adventurersbackpack.registry;

import com.anantaya.adventurersbackpack.AdventurersBackpack;

import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class ModCreativeTabs {

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModCreativeTabs::buildCreativeTabs);
        AdventurersBackpack.LOGGER.info("Backpack creative tabs registered");
    }

    private static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.TOOLS_AND_UTILITIES) {
            return;
        }

        event.accept(ModItems.BACKPACK_IRON.get());
        event.accept(ModItems.BACKPACK_DIAMOND.get());
        event.accept(ModItems.BACKPACK_NETHERITE.get());

        event.accept(ModItems.LANTERN_HOOK.get());
        event.accept(ModItems.AUTO_PICKUP.get());
        event.accept(ModItems.FOOD_POUCH.get());
        event.accept(ModItems.RESTOCK.get());
        event.accept(ModItems.FLUID_STORAGE.get());
        event.accept(ModItems.EXTRA_STORAGE.get());
        event.accept(ModItems.CRAFTING.get());
        event.accept(ModItems.UPGRADE_BASE.get());
    }
}