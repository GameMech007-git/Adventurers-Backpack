package com.anantaya.adventurersbackpack.compat;

import com.anantaya.adventurersbackpack.upgrade.BackpackPlayerUpgradeHelper;

import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

public final class BackpackLanternEntityLuminance implements EntityLuminance {

    private static final int LIGHT_LEVEL = 15;

    @Override
    public @NonNull Type type() {

        return EntityLuminance.Type.VALUE;
    }

    @Override
    public int getLuminance(@NonNull ItemLightSourceManager itemLightSourceManager, @NonNull Entity entity) {
        if (!(entity instanceof Player player)) {
            return 0;
        }

        return BackpackPlayerUpgradeHelper.hasLanternHookBackpack(player)
                ? LIGHT_LEVEL
                : 0;
    }
}