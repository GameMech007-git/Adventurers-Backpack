package com.anantaya.adventurersbackpack.client;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import com.anantaya.adventurersbackpack.network.OpenBackpackPayload;
import com.anantaya.adventurersbackpack.registry.ModMenus;

import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(
        modid = AdventurersBackpack.MOD_ID,
        value = Dist.CLIENT
)
public final class BackpackClient {

    private BackpackClient() {
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        System.out.println("[Backpack] registerMenuScreens fired");

        event.register(ModMenus.BACKPACK_MENU_IRON.get(), BackpackScreen::new);
        event.register(ModMenus.BACKPACK_MENU_DIAMOND.get(), BackpackScreen::new);
        event.register(ModMenus.BACKPACK_MENU_NETHERITE.get(), BackpackScreen::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        System.out.println("[Backpack] registerKeyMappings fired");

        event.register(BackpackKeyBindings.OPEN_BACKPACK);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();

        while (BackpackKeyBindings.OPEN_BACKPACK.consumeClick()) {
            if (client.player == null || client.getConnection() == null) {
                continue;
            }

            if (client.screen instanceof BackpackScreen) {
                client.player.closeContainer();
                continue;
            }

            ClientPacketDistributor.sendToServer(new OpenBackpackPayload());
        }
    }
}