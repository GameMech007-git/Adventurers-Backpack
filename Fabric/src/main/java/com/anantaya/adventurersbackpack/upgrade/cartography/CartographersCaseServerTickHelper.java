package com.anantaya.adventurersbackpack.upgrade.cartography;

import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class CartographersCaseServerTickHelper {

    private static final double REACHED_DISTANCE = 10.0D;

    private CartographersCaseServerTickHelper() {
    }

    public static void tick(ServerPlayer player) {
        if (player == null || player.isSpectator()) {
            return;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.isEmpty() || !(stack.getItem() instanceof BackpackItem backpackItem)) {
                continue;
            }

            BackpackTier tier = backpackItem.getTier();

            if (!BackpackUpgradeHelper.hasUpgrade(
                    stack,
                    BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE,
                    tier,
                    player.level().registryAccess()
            )) {
                continue;
            }

            Optional<CartographersCaseData.Target> targetOptional =
                    CartographersCaseData.getTargetForActiveSlot(
                            stack,
                            player.level().registryAccess()
                    );

            if (targetOptional.isEmpty()) {
                continue;
            }

            CartographersCaseData.Target target = targetOptional.get();

            String playerDimension =
                    player.level().dimension().identifier().toString();

            if (!playerDimension.equals(target.dimension())) {
                continue;
            }

            if (horizontalDistance(player, target) <= REACHED_DISTANCE) {
                CartographersCaseData.setActiveSlot(
                        stack,
                        CartographersCaseHelper.NO_ACTIVE_SLOT
                );

                player.getInventory().setChanged();
            }
        }
    }

    private static double horizontalDistance(
            ServerPlayer player,
            CartographersCaseData.Target target
    ) {
        double dx = target.pos().getX() + 0.5D - player.getX();
        double dz = target.pos().getZ() + 0.5D - player.getZ();

        return Math.sqrt(dx * dx + dz * dz);
    }
}