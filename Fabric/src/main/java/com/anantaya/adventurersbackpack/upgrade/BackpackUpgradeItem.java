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
        CARTOGRAPHERS_CASE,
        NESTED_UPGRADE;

        public boolean hasConfigPanel() {
            return switch (this) {
                case AUTO_PICKUP,
                     FOOD_POUCH,
                     RESTOCK,
                     RECALL_RUNE,
                     CARTOGRAPHERS_CASE,
                     NESTED_UPGRADE -> true;

                case LANTERN_HOOK,
                     FLUID_STORAGE,
                     EXTRA_STORAGE,
                     CRAFTING -> false;
            };
        }

        public boolean canBeNested() {
            return this != NESTED_UPGRADE
                    && !this.hasConfigPanel();
        }
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