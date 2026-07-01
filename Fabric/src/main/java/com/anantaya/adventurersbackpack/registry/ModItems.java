package com.anantaya.adventurersbackpack.registry;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    public static final Item UPGRADE_BASE = registerItem(
            "upgrade_base",
            new Item.Properties()
    );

    public static final Item RECALL_RUNE = registerUpgradeItem(
            "recall_rune",
            BackpackUpgradeItem.Type.RECALL_RUNE
    );

    public static final Item RECALL_SHARD = registerItem(
            "recall_shard",
            new Item.Properties()
    );

    public static final Item CARTOGRAPHERS_CASE =
            registerUpgradeItem(
                    "cartographers_case",
                    BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE
            );

    public static final Item NESTED_UPGRADE = registerUpgradeItem(
            "nested_upgrade",
            BackpackUpgradeItem.Type.NESTED_UPGRADE
    );

    private ModItems() {
    }

    public static void register() {
        AdventurersBackpack.LOGGER.info("Backpack items registered");
    }

    private static Item registerBackpackItem(
            String name,
            BackpackTier tier,
            Block block
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(AdventurersBackpack.MOD_ID, name);

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
        Identifier id = Identifier.fromNamespaceAndPath(AdventurersBackpack.MOD_ID, name);

        Item.Properties properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .stacksTo(1);

        if (type == BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE) {
            properties = properties
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .component(DataComponents.RARITY, Rarity.EPIC);
        }

        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new BackpackUpgradeItem(
                        type,
                        properties
                )
        );
    }

    private static Item registerItem(
            String name,
            Item.Properties properties
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(AdventurersBackpack.MOD_ID, name);

        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new Item(
                        properties.setId(ResourceKey.create(Registries.ITEM, id))
                )
        );
    }
}