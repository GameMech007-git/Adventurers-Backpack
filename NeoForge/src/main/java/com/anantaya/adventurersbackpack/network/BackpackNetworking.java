package com.anantaya.adventurersbackpack.network;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackScreenHandler;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeConfigAction;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BackpackNetworking {

    private BackpackNetworking() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(BackpackNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(AdventurersBackpack.MOD_ID);

        registrar.playToServer(
                OpenBackpackPayload.TYPE,
                OpenBackpackPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }

                    AdventurersBackpack.LOGGER.info(
                            "[Backpack] OpenBackpackPayload received. Current menu = {}",
                            player.containerMenu.getClass().getName()
                    );

                    openBackpackMenu(player);
                })
        );

        registrar.playToServer(
                UpgradeConfigPayload.TYPE,
                UpgradeConfigPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }

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
                })
        );

        registrar.playToServer(
                FluidStoragePayload.TYPE,
                FluidStoragePayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }

                    AbstractContainerMenu menu = player.containerMenu;

                    if (!(menu instanceof BackpackScreenHandler backpackMenu)) {
                        return;
                    }

                    backpackMenu.handleFluidStorageClick(player);
                })
        );

        registrar.playToServer(
                BackpackTrashPayload.TYPE,
                BackpackTrashPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }

                    AbstractContainerMenu menu = player.containerMenu;

                    if (!(menu instanceof BackpackScreenHandler backpackMenu)) {
                        return;
                    }

                    backpackMenu.handleTrashClick();
                })
        );

        registrar.playToServer(
                BackpackSortPayload.TYPE,
                BackpackSortPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }

                    AbstractContainerMenu menu = player.containerMenu;

                    if (!(menu instanceof BackpackScreenHandler backpackMenu)) {
                        return;
                    }

                    backpackMenu.handleSortNormalStorage(player);
                })
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
                foundSlot = i;
                break;
            }
        }

        if (foundStack == null) {
            ItemStack offhand = inv.getItem(40);

            if (!offhand.isEmpty() && offhand.getItem() instanceof BackpackItem) {
                foundStack = offhand;
                foundSlot = 40;
            }
        }

        if (foundStack == null) {
            AdventurersBackpack.LOGGER.warn(
                    "[Backpack] Player {} pressed B but has no backpack!",
                    player.getName().getString()
            );
            return;
        }

        BackpackTier tier = ((BackpackItem) foundStack.getItem()).getTier();
        final int slot = foundSlot;
        final ItemStack ref = foundStack;

        String title = switch (tier) {
            case IRON -> "Backpack";
            case DIAMOND -> "Backpack II";
            case NETHERITE -> "Backpack III";
        };

        player.openMenu(
                new SimpleMenuProvider(
                        (syncId, playerInv, p) -> {
                            ItemStack latest = p.getInventory().getItem(slot);

                            if (latest.isEmpty() || !(latest.getItem() instanceof BackpackItem)) {
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
                ),
                buffer -> buffer.writeInt(slot)
        );
    }
}