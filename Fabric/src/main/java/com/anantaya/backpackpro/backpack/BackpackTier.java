package com.anantaya.backpackpro.backpack;

public enum BackpackTier {

    IRON(5, 27, 2, 3),
    DIAMOND(9, 36, 4, 5),
    NETHERITE(18, 45, 6, 20);

    public static final int MAX_UPGRADE_SLOTS = 6;

    public final int protectedSlots;
    public final int normalSlots;
    public final int upgradeSlots;
    public final int maxDurability;

    public final int totalSlots;

    BackpackTier(
            int protectedSlots,
            int normalSlots,
            int upgradeSlots,
            int maxDurability
    ) {
        this.protectedSlots = protectedSlots;
        this.normalSlots = normalSlots;
        this.upgradeSlots = upgradeSlots;
        this.maxDurability = maxDurability;
        this.totalSlots = protectedSlots + normalSlots + upgradeSlots;
    }

    public int protectedRows() {
        return (int) Math.ceil(this.protectedSlots / 9.0D);
    }

    public int normalRows() {
        return (int) Math.ceil(this.normalSlots / 9.0D);
    }

    public int normalStart() {
        return this.protectedSlots;
    }

    public int upgradeStart() {
        return this.protectedSlots + this.normalSlots;
    }
}