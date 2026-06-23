package com.anantaya.adventurersbackpack.registry;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.block.BackpackBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(AdventurersBackpack.MOD_ID);

    public static final DeferredBlock<BackpackBlock> BACKPACK_IRON_BLOCK = registerBackpackBlock(
            "backpack_iron",
            BackpackTier.IRON
    );

    public static final DeferredBlock<BackpackBlock> BACKPACK_DIAMOND_BLOCK = registerBackpackBlock(
            "backpack_diamond",
            BackpackTier.DIAMOND
    );

    public static final DeferredBlock<BackpackBlock> BACKPACK_NETHERITE_BLOCK = registerBackpackBlock(
            "backpack_netherite",
            BackpackTier.NETHERITE
    );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        AdventurersBackpack.LOGGER.info("Backpack blocks registered");
    }

    private static DeferredBlock<BackpackBlock> registerBackpackBlock(
            String name,
            BackpackTier tier
    ) {
        return BLOCKS.registerBlock(
                name,
                properties -> new BackpackBlock(
                        tier,
                        properties
                                .strength(1.0F)
                                .noOcclusion()
                ),
                () -> BlockBehaviour.Properties.of()
        );
    }
}