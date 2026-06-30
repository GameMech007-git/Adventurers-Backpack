package com.anantaya.adventurersbackpack.client.hud;

import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeHelper;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.cartography.CartographersCaseData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class CartographersCaseHudTarget {

    private CartographersCaseHudTarget() {
    }

    public static Optional<Result> find(Player player, HolderLookup.Provider registries) {
        if (player == null || registries == null) {
            return Optional.empty();
        }

        ItemStack backpack = findBackpackWithCartographersCase(player, registries);

        if (backpack.isEmpty()) {
            return Optional.empty();
        }

        return CartographersCaseData
                .getTargetForActiveSlot(backpack, registries)
                .map(target -> {
                    boolean sameDimension =
                            player.level().dimension().identifier().toString()
                                    .equals(target.dimension());

                    return new Result(
                            target.name(),
                            target.dimension(),
                            target.pos(),
                            sameDimension
                    );
                });
    }

    private static ItemStack findBackpackWithCartographersCase(
            Player player,
            HolderLookup.Provider registries
    ) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.isEmpty() || !(stack.getItem() instanceof BackpackItem backpackItem)) {
                continue;
            }

            BackpackTier tier = backpackItem.getTier();

            if (BackpackUpgradeHelper.hasUpgrade(
                    stack,
                    BackpackUpgradeItem.Type.CARTOGRAPHERS_CASE,
                    tier,
                    player.level().registryAccess()
            )) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public record Result(
            String name,
            String dimension,
            BlockPos pos,
            boolean sameDimension
    ) {
    }
}