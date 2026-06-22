package com.anantaya.adventurersbackpack.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.anantaya.adventurersbackpack.network.OpenBackpackPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackpackClientEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("adventurersbackpack");

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (BackpackKeyBindings.OPEN_BACKPACK.consumeClick()) {
                if (client.screen instanceof BackpackScreen) {
                    client.player.closeContainer();
                    return;
                }

                LOGGER.info("Backpack key pressed! Sending packet to server...");
                ClientPlayNetworking.send(new OpenBackpackPayload());
            }
        });
    }
}