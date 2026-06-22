package com.anantaya.adventurersbackpack.client;

import com.anantaya.adventurersbackpack.network.OpenBackpackPayload;

import net.minecraft.client.Minecraft;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BackpackClientEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("adventurersbackpack");

    private BackpackClientEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BackpackClientEvents::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null) {
            return;
        }

        if (BackpackKeyBindings.OPEN_BACKPACK.consumeClick()) {
            if (client.screen instanceof BackpackScreen) {
                client.player.closeContainer();
                return;
            }

            LOGGER.info("Backpack key pressed! Sending packet to server...");
            ClientPacketDistributor.sendToServer(new OpenBackpackPayload());
        }
    }
}