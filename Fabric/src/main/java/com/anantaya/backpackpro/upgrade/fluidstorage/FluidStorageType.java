package com.anantaya.backpackpro.upgrade.fluidstorage;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum FluidStorageType {
    NONE(0, "none"),
    WATER(1, "water"),
    LAVA(2, "lava"),
    MILK(3, "milk");

    private final int networkId;
    private final String id;

    FluidStorageType(int networkId, String id) {
        this.networkId = networkId;
        this.id = id;
    }

    public int networkId() {
        return networkId;
    }

    public String id() {
        return id;
    }

    public static FluidStorageType byNetworkId(int networkId) {
        for (FluidStorageType type : values()) {
            if (type.networkId == networkId) {
                return type;
            }
        }

        return NONE;
    }

    public static FluidStorageType byId(String id) {
        for (FluidStorageType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }

        return NONE;
    }

    public static FluidStorageType fromFilledContainer(Item item) {
        if (item == Items.WATER_BUCKET) {
            return WATER;
        }

        if (item == Items.LAVA_BUCKET) {
            return LAVA;
        }

        if (item == Items.MILK_BUCKET) {
            return MILK;
        }

        return NONE;
    }

    public Item filledBucketItem() {
        return switch (this) {
            case WATER -> Items.WATER_BUCKET;
            case LAVA -> Items.LAVA_BUCKET;
            case MILK -> Items.MILK_BUCKET;
            case NONE -> Items.BUCKET;
        };
    }
}