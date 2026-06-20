package com.anantaya.backpackpro.compat;

import com.anantaya.backpackpro.upgrade.BackpackPlayerUpgradeHelper;

import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class BackpackLanternEntityLuminance implements EntityLuminance {

    private static final int LIGHT_LEVEL = 15;

    @Override
    public Type type() {
        /*
         * This object is registered directly from Java, not decoded from JSON.
         * Returning VALUE is safe because LambDynamicLights only needs
         * getLuminance(...) at runtime here.
         */
        return EntityLuminance.Type.VALUE;
    }

    @Override
    public int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
        if (!(entity instanceof Player player)) {
            return 0;
        }

        return BackpackPlayerUpgradeHelper.hasLanternHookBackpack(player)
                ? LIGHT_LEVEL
                : 0;
    }
}