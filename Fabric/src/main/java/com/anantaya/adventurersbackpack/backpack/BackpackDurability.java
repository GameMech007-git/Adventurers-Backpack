package com.anantaya.adventurersbackpack.backpack;

public final class BackpackDurability {
    private BackpackDurability() {
    }

    public static int sanitizeDurability(int durability, int maxDurability) {
        return Math.max(0, Math.min(durability, maxDurability));
    }
}
