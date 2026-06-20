package com.anantaya.backpackpro.registry;

import com.anantaya.backpackpro.BackpackPro;
import com.anantaya.backpackpro.block.entity.BackpackBlockEntity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

    public static final BlockEntityType<BackpackBlockEntity> BACKPACK_BLOCK_ENTITY =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(BackpackPro.MOD_ID, "backpack"),
                    FabricBlockEntityTypeBuilder
                            .create(
                                    BackpackBlockEntity::new,
                                    ModBlocks.BACKPACK_IRON_BLOCK,
                                    ModBlocks.BACKPACK_DIAMOND_BLOCK,
                                    ModBlocks.BACKPACK_NETHERITE_BLOCK
                            )
                            .build()
            );

    private ModBlockEntities() {
    }

    public static void register() {
        BackpackPro.LOGGER.info("Backpack block entities registered");
    }
}