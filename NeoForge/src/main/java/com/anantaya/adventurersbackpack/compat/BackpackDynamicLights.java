package com.anantaya.adventurersbackpack.compat;

import com.anantaya.adventurersbackpack.AdventurersBackpack;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;

import net.minecraft.world.entity.EntityType;

public class BackpackDynamicLights implements DynamicLightsInitializer {

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        context.entityLightSourceManager()
                .onRegisterEvent()
                .register(registerContext -> {
                    registerContext.register(
                            EntityType.PLAYER,
                            new BackpackLanternEntityLuminance()
                    );

                    AdventurersBackpack.LOGGER.info(
                            "Adventurer's BackPack Lantern Hook player entity light registered."
                    );
                });

        context.dynamicLightBehaviorManager().add(
                new BackpackLanternDynamicLightBehavior()
        );

        AdventurersBackpack.LOGGER.info(
                "Adventurer's BackPack Lantern Hook custom dynamic light behavior registered."
        );
    }
}