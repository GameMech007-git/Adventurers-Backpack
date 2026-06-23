package com.anantaya.adventurersbackpack.upgrade.recallrune;

import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.registry.ModItems;
import com.anantaya.adventurersbackpack.upgrade.config.RecallRuneConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RecallRunePendingManager {

    private static final int WARMUP_TICKS = 60;
    private static final double MAX_MOVE_DISTANCE_SQR = 0.04D; // 0.2 blocks

    private static final Map<UUID, PendingRecall> PENDING = new HashMap<>();

    private RecallRunePendingManager() {
    }

    public static void startRecall(
            ServerPlayer player,
            Container backpackInventory,
            Container extraStorageInventory,
            BackpackTier tier,
            ItemStack upgradeStack,
            String anchorKey
    ) {
        RecallRuneConfig.Anchor anchor =
                RecallRuneConfig.getAnchor(upgradeStack, anchorKey);

        if (anchor == null) {
            player.sendSystemMessage(Component.literal("No anchor bound."));
            return;
        }

        String currentDimension =
                player.level().dimension().identifier().toString();

        if (!currentDimension.equals(anchor.dimension())) {
            player.sendSystemMessage(Component.literal("Recall Rune only works in the same dimension."));
            return;
        }

        if (isAlreadyAtAnchor(player, anchor)) {
            player.sendSystemMessage(Component.literal("You are already at this anchor."));
            return;
        }

        if (!isSafeRecallTarget(player, anchor)) {
            player.sendSystemMessage(Component.literal("Recall location is unsafe."));
            return;
        }

        if (!canStartRecall(player)) {
            player.sendSystemMessage(Component.literal("You must be standing safely to recall."));
            return;
        }

        PENDING.put(
                player.getUUID(),
                new PendingRecall(
                        player,
                        backpackInventory,
                        extraStorageInventory,
                        tier,
                        upgradeStack,
                        anchor,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        WARMUP_TICKS
                )
        );

        player.sendSystemMessage(Component.literal("Hold still to recall..."));
    }

    public static void tick() {
        Iterator<Map.Entry<UUID, PendingRecall>> iterator =
                PENDING.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingRecall> entry = iterator.next();
            PendingRecall recall = entry.getValue();
            ServerPlayer player = recall.player();

            if (player.isRemoved() || player.isDeadOrDying()) {
                iterator.remove();
                continue;
            }

            if (!canContinueRecall(player, recall)) {
                player.sendSystemMessage(Component.literal("Recall cancelled."));
                iterator.remove();
                continue;
            }

            int ticksLeft = recall.ticksLeft() - 1;

            if (ticksLeft > 0) {
                entry.setValue(recall.withTicksLeft(ticksLeft));
                continue;
            }

            if (!consumeRecallShard(
                    player,
                    recall.backpackInventory(),
                    recall.extraStorageInventory(),
                    recall.tier()
            )) {
                player.sendSystemMessage(Component.literal("A Recall Shard is required."));
                iterator.remove();
                continue;
            }

            teleportNow(player, recall.anchor());
            iterator.remove();
        }
    }

    private static boolean canStartRecall(ServerPlayer player) {
        if (!player.onGround()) {
            return false;
        }

        if (player.fallDistance > 0.0F) {
            return false;
        }

        if (player.isInLava()) {
            return false;
        }

        if (player.getRemainingFireTicks() > 0) {
            return false;
        }

        return true;
    }

    private static boolean canContinueRecall(
            ServerPlayer player,
            PendingRecall recall
    ) {
        if (!player.onGround()) {
            return false;
        }

        if (player.fallDistance > 0.0F) {
            return false;
        }

        if (player.isInLava()) {
            return false;
        }

        if (player.getRemainingFireTicks() > 0) {
            return false;
        }

        double dx = player.getX() - recall.startX();
        double dy = player.getY() - recall.startY();
        double dz = player.getZ() - recall.startZ();

        return dx * dx + dy * dy + dz * dz <= MAX_MOVE_DISTANCE_SQR;
    }

    private static void teleportNow(
            ServerPlayer player,
            RecallRuneConfig.Anchor anchor
    ) {
        player.teleportTo(
                player.level(),
                anchor.x(),
                anchor.y(),
                anchor.z(),
                Set.of(),
                anchor.yaw(),
                anchor.pitch(),
                true
        );

        player.resetFallDistance();
        player.sendSystemMessage(Component.literal("Recalled."));
    }

    private static boolean isAlreadyAtAnchor(
            ServerPlayer player,
            RecallRuneConfig.Anchor anchor
    ) {
        int playerChunkX = ((int) Math.floor(player.getX())) >> 4;
        int playerChunkZ = ((int) Math.floor(player.getZ())) >> 4;

        int anchorChunkX = ((int) Math.floor(anchor.x())) >> 4;
        int anchorChunkZ = ((int) Math.floor(anchor.z())) >> 4;

        return playerChunkX == anchorChunkX
                && playerChunkZ == anchorChunkZ;
    }

    private static boolean isSafeRecallTarget(
            ServerPlayer player,
            RecallRuneConfig.Anchor anchor
    ) {
        AABB targetBox = player.getBoundingBox().move(
                anchor.x() - player.getX(),
                anchor.y() - player.getY(),
                anchor.z() - player.getZ()
        );

        if (!player.level().noCollision(player, targetBox)) {
            return false;
        }

        if (player.level().containsAnyLiquid(targetBox)) {
            return false;
        }

        BlockPos belowPos = BlockPos.containing(
                anchor.x(),
                anchor.y() - 0.1D,
                anchor.z()
        );

        return !player.level().getBlockState(belowPos).isAir();
    }

    private static boolean consumeRecallShard(
            ServerPlayer player,
            Container backpackInventory,
            Container extraStorageInventory,
            BackpackTier tier
    ) {
        if (consumeRecallShardFromPlayer(player)) {
            return true;
        }

        if (consumeRecallShardFromBackpackProtectedAndNormalStorage(
                backpackInventory,
                tier
        )) {
            return true;
        }

        return consumeRecallShardFromExtraStorage(extraStorageInventory);
    }

    private static boolean consumeRecallShardFromPlayer(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.is(ModItems.RECALL_SHARD)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }

        return false;
    }

    private static boolean consumeRecallShardFromBackpackProtectedAndNormalStorage(
            Container backpackInventory,
            BackpackTier tier
    ) {
        for (int i = 0; i < tier.upgradeStart(); i++) {
            ItemStack stack = backpackInventory.getItem(i);

            if (stack.is(ModItems.RECALL_SHARD)) {
                stack.shrink(1);
                backpackInventory.setChanged();
                return true;
            }
        }

        return false;
    }

    private static boolean consumeRecallShardFromExtraStorage(
            Container extraStorageInventory
    ) {
        if (extraStorageInventory == null) {
            return false;
        }

        for (int i = 0; i < extraStorageInventory.getContainerSize(); i++) {
            ItemStack stack = extraStorageInventory.getItem(i);

            if (stack.is(ModItems.RECALL_SHARD)) {
                stack.shrink(1);
                extraStorageInventory.setChanged();
                return true;
            }
        }

        return false;
    }

    private record PendingRecall(
            ServerPlayer player,
            Container backpackInventory,
            Container extraStorageInventory,
            BackpackTier tier,
            ItemStack upgradeStack,
            RecallRuneConfig.Anchor anchor,
            double startX,
            double startY,
            double startZ,
            int ticksLeft
    ) {
        private PendingRecall withTicksLeft(int newTicksLeft) {
            return new PendingRecall(
                    player,
                    backpackInventory,
                    extraStorageInventory,
                    tier,
                    upgradeStack,
                    anchor,
                    startX,
                    startY,
                    startZ,
                    newTicksLeft
            );
        }
    }
}
