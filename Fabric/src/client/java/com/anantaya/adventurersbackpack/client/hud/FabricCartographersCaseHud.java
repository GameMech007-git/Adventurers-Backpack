package com.anantaya.adventurersbackpack.client.hud;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public final class FabricCartographersCaseHud {

    private FabricCartographersCaseHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(
                        AdventurersBackpack.MOD_ID,
                        "cartographers_case_hud"
                ),
                (graphics, deltaTracker) ->
                        CartographersCaseHudRenderer.extractRenderState(graphics)
        );
    }
}