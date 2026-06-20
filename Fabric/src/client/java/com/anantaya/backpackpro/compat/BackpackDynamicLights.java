package com.anantaya.backpackpro.compat;

import com.anantaya.backpackpro.BackpackPro;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class BackpackDynamicLights implements DynamicLightsInitializer {

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        context.entityLightSourceManager()
                .onRegisterEvent()
                .register(
                        Identifier.fromNamespaceAndPath(
                                BackpackPro.MOD_ID,
                                "lantern_hook_player"
                        ),
                        registerContext -> {
                            registerContext.register(
                                    EntityType.PLAYER,
                                    new BackpackLanternEntityLuminance()
                            );

                            BackpackPro.LOGGER.info(
                                    "Backpack Pro Lantern Hook player entity light registered."
                            );
                        }
                );
    }
}