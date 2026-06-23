package com.anantaya.adventurersbackpack.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.world.item.CreativeModeTabs;

public final class ModCreativeTabs {

    private ModCreativeTabs() {
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> {
                    output.accept(ModItems.BACKPACK_IRON);
                    output.accept(ModItems.BACKPACK_DIAMOND);
                    output.accept(ModItems.BACKPACK_NETHERITE);

                    output.accept(ModItems.LANTERN_HOOK);
                    output.accept(ModItems.AUTO_PICKUP);
                    output.accept(ModItems.FOOD_POUCH);
                    output.accept(ModItems.RESTOCK);
                    output.accept(ModItems.FLUID_STORAGE);
                    output.accept(ModItems.EXTRA_STORAGE);
                    output.accept(ModItems.CRAFTING);
                    output.accept(ModItems.UPGRADE_BASE);
                    output.accept(ModItems.RECALL_RUNE);
                    output.accept(ModItems.RECALL_SHARD);

                });
    }
}