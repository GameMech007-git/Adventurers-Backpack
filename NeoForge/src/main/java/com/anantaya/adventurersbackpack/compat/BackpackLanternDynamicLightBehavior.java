package com.anantaya.adventurersbackpack.compat;

import com.anantaya.adventurersbackpack.upgrade.BackpackPlayerUpgradeHelper;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

public final class BackpackLanternDynamicLightBehavior implements DynamicLightBehavior {

    private static final int RADIUS_XZ = 8;
    private static final int RADIUS_Y = 4;

    private BlockPos lastPlayerPos = BlockPos.ZERO;

    @Override
    public double lightAtPos(@NonNull BlockPos pos, double currentLight) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return currentLight;
        }

        Player player = minecraft.player;

        if (!BackpackPlayerUpgradeHelper.hasLanternHookBackpack(player)) {
            return currentLight;
        }

        BlockPos playerPos = player.blockPosition();

        double dx = pos.getX() - playerPos.getX();
        double dy = pos.getY() - playerPos.getY();
        double dz = pos.getZ() - playerPos.getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double lanternLight = 15.0D - distance * 0.55D;

        if (lanternLight < 0.0D) {
            lanternLight = 0.0D;
        }

        return Math.max(currentLight, lanternLight);
    }

    @Override
    public @NonNull BoundingBox getBoundingBox() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return new BoundingBox(0, 0, 0, 0, 0, 0);
        }

        BlockPos pos = minecraft.player.blockPosition();
        this.lastPlayerPos = pos;

        return new BoundingBox(
                pos.getX() - RADIUS_XZ,
                pos.getY() - RADIUS_Y,
                pos.getZ() - RADIUS_XZ,
                pos.getX() + RADIUS_XZ,
                pos.getY() + RADIUS_Y,
                pos.getZ() + RADIUS_XZ
        );
    }

    @Override
    public boolean hasChanged() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return false;
        }

        return !minecraft.player.blockPosition().equals(this.lastPlayerPos);
    }
}