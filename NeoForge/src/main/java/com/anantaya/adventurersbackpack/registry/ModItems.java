package com.anantaya.adventurersbackpack.registry;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(AdventurersBackpack.MOD_ID);

    public static final DeferredItem<BackpackItem> BACKPACK_IRON = registerBackpackItem(
            "backpack_iron",
            BackpackTier.IRON,
            ModBlocks.BACKPACK_IRON_BLOCK
    );

    public static final DeferredItem<BackpackItem> BACKPACK_DIAMOND = registerBackpackItem(
            "backpack_diamond",
            BackpackTier.DIAMOND,
            ModBlocks.BACKPACK_DIAMOND_BLOCK
    );

    public static final DeferredItem<BackpackItem> BACKPACK_NETHERITE = registerBackpackItem(
            "backpack_netherite",
            BackpackTier.NETHERITE,
            ModBlocks.BACKPACK_NETHERITE_BLOCK
    );

    public static final DeferredItem<BackpackUpgradeItem> LANTERN_HOOK = registerUpgradeItem(
            "lantern_hook",
            BackpackUpgradeItem.Type.LANTERN_HOOK
    );

    public static final DeferredItem<BackpackUpgradeItem> AUTO_PICKUP = registerUpgradeItem(
            "auto_pickup",
            BackpackUpgradeItem.Type.AUTO_PICKUP
    );

    public static final DeferredItem<BackpackUpgradeItem> FOOD_POUCH = registerUpgradeItem(
            "food_pouch",
            BackpackUpgradeItem.Type.FOOD_POUCH
    );

    public static final DeferredItem<BackpackUpgradeItem> RESTOCK = registerUpgradeItem(
            "restock",
            BackpackUpgradeItem.Type.RESTOCK
    );

    public static final DeferredItem<BackpackUpgradeItem> FLUID_STORAGE = registerUpgradeItem(
            "fluid_storage",
            BackpackUpgradeItem.Type.FLUID_STORAGE
    );

    public static final DeferredItem<BackpackUpgradeItem> EXTRA_STORAGE = registerUpgradeItem(
            "extra_storage",
            BackpackUpgradeItem.Type.EXTRA_STORAGE
    );

    public static final DeferredItem<BackpackUpgradeItem> CRAFTING = registerUpgradeItem(
            "crafting",
            BackpackUpgradeItem.Type.CRAFTING
    );

    public static final DeferredItem<BackpackUpgradeItem> RECALL_RUNE= registerUpgradeItem(
            "recall_rune",
            BackpackUpgradeItem.Type.RECALL_RUNE
    );

    public static final DeferredItem<Item> UPGRADE_BASE =
            ITEMS.registerSimpleItem("upgrade_base");

    public static final DeferredItem<Item> RECALL_SHARD=
            ITEMS.registerSimpleItem("recall_shard");

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        AdventurersBackpack.LOGGER.info("Backpack items registered");
    }

    private static DeferredItem<BackpackItem> registerBackpackItem(
            String name,
            BackpackTier tier,
            Supplier<? extends Block> block
    ) {
        return ITEMS.registerItem(
                name,
                properties -> new BackpackItem(
                        tier,
                        block.get(),
                        properties.stacksTo(1)
                )
        );
    }

    private static DeferredItem<BackpackUpgradeItem> registerUpgradeItem(
            String name,
            BackpackUpgradeItem.Type type
    ) {
        return ITEMS.registerItem(
                name,
                properties -> new BackpackUpgradeItem(
                        type,
                        properties.stacksTo(1)
                )
        );
    }
}