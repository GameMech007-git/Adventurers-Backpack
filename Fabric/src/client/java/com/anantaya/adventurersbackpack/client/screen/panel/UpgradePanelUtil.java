package com.anantaya.adventurersbackpack.client.screen.panel;

import net.minecraft.resources.Identifier;

public final class UpgradePanelUtil {

    public static final String MOD_ID = "adventurersbackpack";

    private UpgradePanelUtil() {
    }

    public static Identifier texture(String path) {
        return Identifier.fromNamespaceAndPath(
                MOD_ID,
                "textures/gui/" + path + ".png"
        );
    }

    public static boolean isInside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int w,
            int h
    ) {
        return mouseX >= x
                && mouseX < x + w
                && mouseY >= y
                && mouseY < y + h;
    }
}