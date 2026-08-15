package com.anantaya.adventurersbackpack.backpack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;

public final class BackpackPlacementDecision {
    private BackpackPlacementDecision() {
    }

    public static boolean shouldOpenBackpackMenu(InteractionResult placementResult) {
        return placementResult == InteractionResult.PASS
                || placementResult == InteractionResult.FAIL;
    }

    public static boolean shouldDeferToPlacement(Level level, BlockPos clickedPos, Direction clickedFace) {
        if (level == null || clickedPos == null) {
            return false;
        }

        if (level.getFluidState(clickedPos).is(FluidTags.WATER)) {
            return true;
        }

        if (clickedFace != null) {
            BlockPos adjacentPos = clickedPos.relative(clickedFace);
            return level.getFluidState(adjacentPos).is(FluidTags.WATER);
        }

        return false;
    }
}
