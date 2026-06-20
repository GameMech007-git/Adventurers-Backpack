package com.anantaya.backpackpro.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BackpackUnloadTargetHelper {

    private BackpackUnloadTargetHelper() {
    }

    public static @Nullable Container resolveTargetContainer(
            Level level,
            BlockPos pos
    ) {
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(
                    chestBlock,
                    state,
                    level,
                    pos,
                    true
            );
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof Container container) {
            return container;
        }

        return null;
    }
}