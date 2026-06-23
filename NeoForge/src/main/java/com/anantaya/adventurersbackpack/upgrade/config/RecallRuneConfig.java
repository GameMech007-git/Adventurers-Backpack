package com.anantaya.adventurersbackpack.upgrade.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class RecallRuneConfig {

    public static final String HOME = "RecallHome";
    public static final String WAYPOINT_1 = "RecallWaypoint1";
    public static final String WAYPOINT_2 = "RecallWaypoint2";

    private static final String KEY_DIMENSION = "Dimension";
    private static final String KEY_X = "X";
    private static final String KEY_Y = "Y";
    private static final String KEY_Z = "Z";
    private static final String KEY_YAW = "Yaw";
    private static final String KEY_PITCH = "Pitch";

    private RecallRuneConfig() {
    }

    public static void bindAnchor(
            ItemStack upgradeStack,
            String anchorKey,
            ServerPlayer player
    ) {
        CompoundTag root = BackpackUpgradeDataHelper.getTag(upgradeStack);

        CompoundTag anchor = new CompoundTag();
        anchor.putString(KEY_DIMENSION, player.level().dimension().identifier().toString());
        anchor.putDouble(KEY_X, player.getX());
        anchor.putDouble(KEY_Y, player.getY());
        anchor.putDouble(KEY_Z, player.getZ());
        anchor.putFloat(KEY_YAW, player.getYRot());
        anchor.putFloat(KEY_PITCH, player.getXRot());

        root.put(anchorKey, anchor);
        BackpackUpgradeDataHelper.saveTag(upgradeStack, root);
    }

    public static boolean hasAnchor(
            ItemStack upgradeStack,
            String anchorKey
    ) {
        CompoundTag root = BackpackUpgradeDataHelper.getTag(upgradeStack);
        return root.contains(anchorKey);
    }

    public static Anchor getAnchor(
            ItemStack upgradeStack,
            String anchorKey
    ) {
        CompoundTag root = BackpackUpgradeDataHelper.getTag(upgradeStack);

        if (!root.contains(anchorKey)) {
            return null;
        }

        CompoundTag anchor = root.getCompound(anchorKey).orElse(null);

        if (anchor == null) {
            return null;
        }

        String dimension = anchor.getString(KEY_DIMENSION).orElse("");
        double x = anchor.getDouble(KEY_X).orElse(0.0D);
        double y = anchor.getDouble(KEY_Y).orElse(0.0D);
        double z = anchor.getDouble(KEY_Z).orElse(0.0D);
        float yaw = anchor.getFloat(KEY_YAW).orElse(0.0F);
        float pitch = anchor.getFloat(KEY_PITCH).orElse(0.0F);

        if (dimension.isEmpty()) {
            return null;
        }

        return new Anchor(
                dimension,
                x,
                y,
                z,
                yaw,
                pitch
        );
    }

    public record Anchor(
            String dimension,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
    }
}