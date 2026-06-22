package com.anantaya.adventurersbackpack.registry;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import com.anantaya.adventurersbackpack.block.entity.BackpackBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    AdventurersBackpack.MOD_ID
            );

    public static final Supplier<BlockEntityType<BackpackBlockEntity>> BACKPACK_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "backpack",
                    () -> new BlockEntityType<>(
                            BackpackBlockEntity::new,
                            ModBlocks.BACKPACK_IRON_BLOCK.get(),
                            ModBlocks.BACKPACK_DIAMOND_BLOCK.get(),
                            ModBlocks.BACKPACK_NETHERITE_BLOCK.get()
                    )
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
        AdventurersBackpack.LOGGER.info("Backpack block entities registered");
    }
}