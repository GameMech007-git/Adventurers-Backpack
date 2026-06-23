package com.anantaya.adventurersbackpack.client;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public final class BackpackKeyBindings {

    private static final KeyMapping.Category BACKPACK_CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath(
                            AdventurersBackpack.MOD_ID,
                            "general"
                    )
            );

    public static final KeyMapping OPEN_BACKPACK =
            new KeyMapping(
                    "key.adventurersbackpack.open_backpack",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_B,
                    BACKPACK_CATEGORY
            );

    private BackpackKeyBindings() {
    }
}