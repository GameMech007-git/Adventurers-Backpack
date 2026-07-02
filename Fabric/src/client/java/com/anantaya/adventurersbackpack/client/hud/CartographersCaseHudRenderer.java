package com.anantaya.adventurersbackpack.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class CartographersCaseHudRenderer {

    private CartographersCaseHudRenderer() {
    }

    public static void extractRenderState(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        Player player = minecraft.player;

        Optional<CartographersCaseHudTarget.Result> resultOptional =
                CartographersCaseHudTarget.find(
                        player,
                        minecraft.level.registryAccess()
                );

        if (resultOptional.isEmpty()) {
            return;
        }

        CartographersCaseHudTarget.Result result = resultOptional.get();

        if (result.sameDimension() && horizontalDistance(player, result) <= 10.0D) {
            return;
        }

        String text;

        if (!result.sameDimension()) {
            text = result.name() + ": no signal";
        } else {
            text = result.name() + " " + directionText(player, result);
        }

        drawHudText(
                graphics,
                minecraft.font,
                text
        );
    }

    private static double horizontalDistance(
            Player player,
            CartographersCaseHudTarget.Result result
    ) {
        double dx = result.pos().getX() + 0.5D - player.getX();
        double dz = result.pos().getZ() + 0.5D - player.getZ();

        return Math.sqrt(dx * dx + dz * dz);
    }

    private static void drawHudText(
            GuiGraphicsExtractor graphics,
            Font font,
            String text
    ) {
        int textWidth = font.width(text);

        int x = (graphics.guiWidth() - textWidth) / 2;
        int y = 8;

        graphics.fill(
                x - 5,
                y - 3,
                x + textWidth + 5,
                y + 11,
                0x88000000
        );

        graphics.text(
                font,
                text,
                x,
                y,
                0xFFE6E6E6,
                true
        );
    }

    private static String directionText(
            Player player,
            CartographersCaseHudTarget.Result result
    ) {
        double dx = result.pos().getX() + 0.5D - player.getX();
        double dz = result.pos().getZ() + 0.5D - player.getZ();

        double distance = Math.sqrt(dx * dx + dz * dz);

        String direction = cardinalDirection(dx, dz);

        return direction + " " + Math.round(distance) + "m";
    }

    private static String cardinalDirection(double dx, double dz) {
        double absX = Math.abs(dx);
        double absZ = Math.abs(dz);

        if (absX < 3.0D && absZ < 3.0D) {
            return "here";
        }

        if (absX > absZ * 1.8D) {
            return dx > 0.0D ? "E" : "W";
        }

        if (absZ > absX * 1.8D) {
            return dz > 0.0D ? "S" : "N";
        }

        if (dx > 0.0D && dz > 0.0D) {
            return "SE";
        }

        if (dx > 0.0D) {
            return "NE";
        }

        if (dz > 0.0D) {
            return "SW";
        }

        return "NW";
    }
}