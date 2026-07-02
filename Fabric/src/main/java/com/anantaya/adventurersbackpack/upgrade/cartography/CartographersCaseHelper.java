package com.anantaya.adventurersbackpack.upgrade.cartography;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;

import java.util.Optional;

public final class CartographersCaseHelper {

    public static final int DEATH_SIGNAL_SLOT = 0;
    public static final int FIRST_COMPASS_SLOT = 1;
    public static final int LAST_COMPASS_SLOT = 8;
    public static final int LODESTONE_COMPASS_SLOT_COUNT = 8;
    public static final int TOTAL_NAVIGATION_ENTRIES = 9;
    public static final int NO_ACTIVE_SLOT = -1;

    private CartographersCaseHelper() {
    }

    public static boolean isValidLodestoneCompass(ItemStack stack) {
        return getLodestoneTarget(stack).isPresent();
    }

    public static Optional<GlobalPos> getLodestoneTarget(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        if (!stack.is(Items.COMPASS)) {
            return Optional.empty();
        }

        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null) {
            return Optional.empty();
        }

        return tracker.target();
    }

    public static String getCompassDisplayName(ItemStack stack, String fallback) {
        if (stack == null || stack.isEmpty()) {
            return fallback;
        }

        return stack.getHoverName().getString();
    }

    public static boolean isCompassSlotIndex(int navigationSlot) {
        return navigationSlot >= FIRST_COMPASS_SLOT && navigationSlot <= LAST_COMPASS_SLOT;
    }

    public static int toCompassInventoryIndex(int navigationSlot) {
        return navigationSlot - FIRST_COMPASS_SLOT;
    }

    public static int toNavigationSlot(int compassInventoryIndex) {
        return compassInventoryIndex + FIRST_COMPASS_SLOT;
    }
}