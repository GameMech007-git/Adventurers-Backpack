package com.anantaya.adventurersbackpack.client;

import com.anantaya.adventurersbackpack.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class BackpackClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.BACKPACK_MENU_IRON, BackpackScreen::new);
        MenuScreens.register(ModMenus.BACKPACK_MENU_DIAMOND, BackpackScreen::new);
        MenuScreens.register(ModMenus.BACKPACK_MENU_NETHERITE, BackpackScreen::new);

        BackpackKeyBindings.register();
    }
}