package com.anantaya.adventurersbackpack.upgrade;

import net.minecraft.world.item.Item;

public class BackpackUpgradeItem extends Item {

    public enum Type {
        LANTERN_HOOK,
        AUTO_PICKUP,
        FOOD_POUCH,
        RESTOCK,
        FLUID_STORAGE,
        EXTRA_STORAGE,
        CRAFTING,
        RECALL_RUNE,
        CARTOGRAPHERS_CASE

    }

    private final Type type;

    public BackpackUpgradeItem(Type type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}