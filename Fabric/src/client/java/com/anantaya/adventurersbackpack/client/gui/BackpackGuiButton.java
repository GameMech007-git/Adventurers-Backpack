package com.anantaya.adventurersbackpack.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class BackpackGuiButton {

    public final BackpackGuiButtonType type;
    public final int x;
    public final int y;
    public final int size;
    public final Identifier icon;
    public final Component tooltip;
    public final int data;

    public BackpackGuiButton(
            BackpackGuiButtonType type,
            int x,
            int y,
            int size,
            Identifier icon,
            Component tooltip
    ) {
        this(type, x, y, size, icon, tooltip, -1);
    }

    public BackpackGuiButton(
            BackpackGuiButtonType type,
            int x,
            int y,
            int size,
            Identifier icon,
            Component tooltip,
            int data
    ) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.size = size;
        this.icon = icon;
        this.tooltip = tooltip;
        this.data = data;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x
                && mouseX < x + size
                && mouseY >= y
                && mouseY < y + size;
    }
}