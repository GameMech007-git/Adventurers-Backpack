package com.anantaya.adventurersbackpack.client.gui;

import net.minecraft.resources.Identifier;

public final class BackpackGuiIcons {

    private static final String MOD_ID = "adventurersbackpack";

    public static final Identifier SORT = icon("sort");
    public static final Identifier ARROW_DOWN = icon("arrow_down");
    public static final Identifier ARROW_UP = icon("arrow_up");
    public static final Identifier GEAR = icon("gear");

    private BackpackGuiIcons() {
    }

    private static Identifier icon(String name) {
        return Identifier.fromNamespaceAndPath(
                MOD_ID,
                "textures/gui/icons/" + name + ".png"
        );
    }
}