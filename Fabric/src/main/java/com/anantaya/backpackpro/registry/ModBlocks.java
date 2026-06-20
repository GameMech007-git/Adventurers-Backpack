package com.anantaya.backpackpro.registry;

import com.anantaya.backpackpro.BackpackPro;
import com.anantaya.backpackpro.backpack.BackpackTier;
import com.anantaya.backpackpro.block.BackpackBlock;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {

    public static final Block BACKPACK_IRON_BLOCK = registerBackpackBlock(
            "backpack_iron",
            BackpackTier.IRON
    );

    public static final Block BACKPACK_DIAMOND_BLOCK = registerBackpackBlock(
            "backpack_diamond",
            BackpackTier.DIAMOND
    );

    public static final Block BACKPACK_NETHERITE_BLOCK = registerBackpackBlock(
            "backpack_netherite",
            BackpackTier.NETHERITE
    );

    private ModBlocks() {
    }

    public static void register() {
        BackpackPro.LOGGER.info("Backpack blocks registered");
    }

    private static Block registerBackpackBlock(String name, BackpackTier tier) {
        Identifier id = Identifier.fromNamespaceAndPath(BackpackPro.MOD_ID, name);

        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                new BackpackBlock(
                        tier,
                        BlockBehaviour.Properties.of()
                                .setId(ResourceKey.create(Registries.BLOCK, id))
                                .strength(1.0F)
                                .noOcclusion()
                )
        );
    }
}