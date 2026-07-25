package com.anantaya.adventurersbackpack.network;

import com.anantaya.adventurersbackpack.*;
import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackScreenHandler;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class BackpackNetworking {

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(OpenBackpackPayload.TYPE, OpenBackpackPayload.CODEC);
        
        ServerPlayNetworking.registerGlobalReceiver(
                OpenBackpackPayload.TYPE,
                (payload, context) -> {

                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        AdventurersBackpack.LOGGER.info(
                                "[Backpack] OpenBackpackPayload received. Current menu = {}",
                                player.containerMenu.getClass().getName()
                        );

                        openBackpackMenu(player);
                    });
                }
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpgradeConfigPayload.TYPE,
                UpgradeConfigPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpgradeConfigPayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        AbstractContainerMenu menu = player.containerMenu;

                        if (!(menu instanceof BackpackScreenHandler backpackMenu)) {
                            return;
                        }

                        BackpackUpgradeConfigAction action =
                                BackpackUpgradeConfigAction.byId(payload.actionId());

                        backpackMenu.handleUpgradeConfigAction(
                                payload.upgradeSlotIndex(),
                                action
                        );
                    });
                }
        );

        PayloadTypeRegistry.serverboundPlay().register(
                FluidStoragePayload.TYPE,
                FluidStoragePayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                FluidStoragePayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        AbstractContainerMenu menu = player.containerMenu;

                        if (!(menu instanceof BackpackScreenHandler backpackMenu)) {
                            return;
                        }

                        backpackMenu.handleFluidStorageClick(player);
                    });
                }
        );

        PayloadTypeRegistry.serverboundPlay().register(
                BackpackTrashPayload.TYPE,
                BackpackTrashPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BackpackTrashPayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        AbstractContainerMenu menu = player.containerMenu;

                        if (!(menu instanceof BackpackScreenHandler backpackMenu)) {
                            return;
                        }

                        backpackMenu.handleTrashClick();
                    });
                }
        );

        PayloadTypeRegistry.serverboundPlay().register(
                BackpackSortPayload.TYPE,
                BackpackSortPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BackpackSortPayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        AbstractContainerMenu menu = player.containerMenu;

                        if (!(menu instanceof BackpackScreenHandler backpackMenu)) {
                            return;
                        }

                        backpackMenu.handleSortNormalStorage(player);
                    });
                }
        );

        PayloadTypeRegistry.serverboundPlay().register(
                CartographersCasePayload.TYPE,
                CartographersCasePayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                CartographersCasePayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        AbstractContainerMenu menu = player.containerMenu;

                        if (!(menu instanceof BackpackScreenHandler backpackMenu)) {
                            return;
                        }

                        backpackMenu.handleCartographersCaseClick(
                                player,
                                payload.upgradeSlotIndex(),
                                payload.navigationSlot()
                        );
                    });
                }
        );
    }





    private static void openBackpackMenu(ServerPlayer player) {
        if (player.containerMenu instanceof BackpackScreenHandler) {
            player.closeContainer();
            return;
        }

        Inventory inv = player.getInventory();

        ItemStack foundStack = null;
        int foundSlot = -1;


        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
                foundStack = stack;
                foundSlot  = i;
                break;
            }
        }

        if (foundStack == null) {
            ItemStack offhand = inv.getItem(40);
            if (!offhand.isEmpty() && offhand.getItem() instanceof BackpackItem) {
                foundStack = offhand;
                foundSlot  = 40;
            }
        }

        if (foundStack == null) {
            AdventurersBackpack.LOGGER.warn("[Backpack] Player {} pressed B but has no backpack!",
                    player.getName().getString());
            return;
        }

        BackpackTier tier   = ((BackpackItem) foundStack.getItem()).getTier();
        final int slot      = foundSlot;
        final ItemStack ref = foundStack;

        String title = switch (tier) {
            case IRON      -> "Backpack";
            case DIAMOND   -> "Backpack II";
            case NETHERITE -> "Backpack III";
        };

        player.openMenu(new SimpleMenuProvider(
                (syncId, playerInv, p) -> {
                    ItemStack latest = p.getInventory().getItem(slot);
                    if (latest.isEmpty()
                            || !(latest.getItem() instanceof BackpackItem)
                            || !ItemStack.isSameItemSameComponents(latest, ref)) {
                        latest = ref;
                    }
                    return new BackpackScreenHandler(
                            syncId,
                            playerInv,
                            latest,
                            tier,
                            p.level().registryAccess(),
                            slot
                    );
                },
                Component.literal(title)
        ));
    }
}