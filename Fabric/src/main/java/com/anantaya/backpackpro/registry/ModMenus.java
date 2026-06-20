package com.anantaya.backpackpro.registry;

import com.anantaya.backpackpro.BackpackPro;
import com.anantaya.backpackpro.backpack.BackpackScreenHandler;
import com.anantaya.backpackpro.backpack.BackpackTier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final MenuType<BackpackScreenHandler> BACKPACK_MENU_IRON =
            registerBackpackMenu("backpack_iron", BackpackTier.IRON);

    public static final MenuType<BackpackScreenHandler> BACKPACK_MENU_DIAMOND =
            registerBackpackMenu("backpack_diamond", BackpackTier.DIAMOND);

    public static final MenuType<BackpackScreenHandler> BACKPACK_MENU_NETHERITE =
            registerBackpackMenu("backpack_netherite", BackpackTier.NETHERITE);

    private ModMenus() {
    }

    public static void register() {
        BackpackPro.LOGGER.info("Backpack menus registered");
    }

    private static MenuType<BackpackScreenHandler> registerBackpackMenu(
            String name,
            BackpackTier tier
    ) {
        return Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(BackpackPro.MOD_ID, name),
                new MenuType<>(
                        (syncId, inv) -> new BackpackScreenHandler(
                                syncId,
                                inv,
                                inv.player.getMainHandItem(),
                                tier,
                                inv.player.level().registryAccess()
                        ),
                        FeatureFlags.DEFAULT_FLAGS
                )
        );
    }
}