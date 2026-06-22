package com.anantaya.adventurersbackpack.registry;

import com.anantaya.adventurersbackpack.AdventurersBackpack;
import com.anantaya.adventurersbackpack.backpack.BackpackScreenHandler;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(
                    Registries.MENU,
                    AdventurersBackpack.MOD_ID
            );

    public static final Supplier<MenuType<BackpackScreenHandler>> BACKPACK_MENU_IRON =
            registerBackpackMenu("backpack_iron", BackpackTier.IRON);

    public static final Supplier<MenuType<BackpackScreenHandler>> BACKPACK_MENU_DIAMOND =
            registerBackpackMenu("backpack_diamond", BackpackTier.DIAMOND);

    public static final Supplier<MenuType<BackpackScreenHandler>> BACKPACK_MENU_NETHERITE =
            registerBackpackMenu("backpack_netherite", BackpackTier.NETHERITE);

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
        AdventurersBackpack.LOGGER.info("Backpack menus registered");
    }

    private static Supplier<MenuType<BackpackScreenHandler>> registerBackpackMenu(
            String name,
            BackpackTier tier
    ) {
        return MENUS.register(
                name,
                () -> new MenuType<>(
                        new BackpackMenuFactory(tier),
                        FeatureFlags.DEFAULT_FLAGS
                )
        );
    }

    private record BackpackMenuFactory(
            BackpackTier tier
    ) implements IContainerFactory<BackpackScreenHandler> {

        @Override
        public BackpackScreenHandler create(
                int syncId,
                Inventory inventory,
                RegistryFriendlyByteBuf data
        ) {
            System.out.println("[Backpack] CLIENT menu factory fired for tier=" + tier);
            System.out.println("[Backpack] CLIENT extra data null = " + (data == null));

            int sourceSlot = data != null ? data.readInt() : -1;

            System.out.println("[Backpack] CLIENT sourceSlot = " + sourceSlot);
            System.out.println("[Backpack] CLIENT inventory size = " + inventory.getContainerSize());

            ItemStack stack = ItemStack.EMPTY;

            if (sourceSlot >= 0 && sourceSlot < inventory.getContainerSize()) {
                stack = inventory.getItem(sourceSlot);
            }

            System.out.println("[Backpack] CLIENT stack = " + stack);
            System.out.println("[Backpack] CLIENT stack item = " + stack.getItem());

            return new BackpackScreenHandler(
                    syncId,
                    inventory,
                    stack,
                    tier,
                    inventory.player.level().registryAccess(),
                    sourceSlot
            );
        }
    }
}