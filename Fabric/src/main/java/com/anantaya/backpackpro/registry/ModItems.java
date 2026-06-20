package com.anantaya.backpackpro.registry;

import com.anantaya.backpackpro.backpack.BackpackItem;
import com.anantaya.backpackpro.BackpackPro;
import com.anantaya.backpackpro.backpack.BackpackTier;
import com.anantaya.backpackpro.upgrade.BackpackUpgradeItem;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModItems {

    public static final Item BACKPACK_IRON = registerBackpackItem(
            "backpack_iron",
            BackpackTier.IRON,
            ModBlocks.BACKPACK_IRON_BLOCK
    );

    public static final Item BACKPACK_DIAMOND = registerBackpackItem(
            "backpack_diamond",
            BackpackTier.DIAMOND,
            ModBlocks.BACKPACK_DIAMOND_BLOCK
    );

    public static final Item BACKPACK_NETHERITE = registerBackpackItem(
            "backpack_netherite",
            BackpackTier.NETHERITE,
            ModBlocks.BACKPACK_NETHERITE_BLOCK
    );

    public static final Item LANTERN_HOOK = registerUpgradeItem(
            "lantern_hook",
            BackpackUpgradeItem.Type.LANTERN_HOOK
    );

    public static final Item AUTO_PICKUP = registerUpgradeItem(
            "auto_pickup",
            BackpackUpgradeItem.Type.AUTO_PICKUP
    );

    public static final Item FOOD_POUCH = registerUpgradeItem(
            "food_pouch",
            BackpackUpgradeItem.Type.FOOD_POUCH
    );

    public static final Item RESTOCK = registerUpgradeItem(
            "restock",
            BackpackUpgradeItem.Type.RESTOCK
    );

    public static final Item FLUID_STORAGE = registerUpgradeItem(
            "fluid_storage",
            BackpackUpgradeItem.Type.FLUID_STORAGE
    );

    public static final Item EXTRA_STORAGE = registerUpgradeItem(
            "extra_storage",
            BackpackUpgradeItem.Type.EXTRA_STORAGE
    );

    public static final Item CRAFTING = registerUpgradeItem(
            "crafting",
            BackpackUpgradeItem.Type.CRAFTING
    );

    private ModItems() {
    }

    public static void register() {
        BackpackPro.LOGGER.info("Backpack items registered");
    }

    private static Item registerBackpackItem(
            String name,
            BackpackTier tier,
            Block block
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(BackpackPro.MOD_ID, name);

        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new BackpackItem(
                        tier,
                        block,
                        new Item.Properties()
                                .setId(ResourceKey.create(Registries.ITEM, id))
                                .stacksTo(1)
                )
        );
    }

    private static Item registerUpgradeItem(
            String name,
            BackpackUpgradeItem.Type type
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(BackpackPro.MOD_ID, name);

        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new BackpackUpgradeItem(
                        type,
                        new Item.Properties()
                                .setId(ResourceKey.create(Registries.ITEM, id))
                                .stacksTo(1)
                )
        );
    }
}