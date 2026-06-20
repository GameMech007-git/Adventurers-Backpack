package com.anantaya.backpackpro.client.gui;

import net.minecraft.resources.Identifier;

public final class BackpackGuiTextures {

    private static final String MOD_ID = "backpack-pro";

    public static final Identifier AUTO_MODE_OFF =
            tex("config/auto_mode_off");

    public static final Identifier AUTO_MODE_MATCHING =
            tex("config/auto_mode_matching");

    public static final Identifier AUTO_MODE_ALL =
            tex("config/auto_mode_all");

    public static final Identifier IGNORE_DROPS_ON =
            tex("config/ignore_drops_on");

    public static final Identifier IGNORE_DROPS_OFF =
            tex("config/ignore_drops_off");

    private BackpackGuiTextures() {
    }

    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath(
                MOD_ID,
                "textures/gui/" + name + ".png"
        );
    }
}