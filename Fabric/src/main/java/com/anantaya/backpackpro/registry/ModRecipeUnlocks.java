package com.anantaya.backpackpro.registry;

import com.anantaya.backpackpro.BackpackPro;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public final class ModRecipeUnlocks {

    private ModRecipeUnlocks() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            List<RecipeHolder<?>> recipes = new ArrayList<>();

            addRecipeIfPresent(server, recipes, "backpack_iron");
            addRecipeIfPresent(server, recipes, "backpack_diamond");
            addRecipeIfPresent(server, recipes, "backpack_netherite");

            player.awardRecipes(recipes);
        });
    }

    private static void addRecipeIfPresent(
            net.minecraft.server.MinecraftServer server,
            List<RecipeHolder<?>> recipes,
            String name
    ) {
        server.getRecipeManager().byKey(
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(BackpackPro.MOD_ID, name)
                )
        ).ifPresent(recipes::add);
    }
}