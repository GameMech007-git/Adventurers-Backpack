package com.anantaya.backpackpro.upgrade.autopickup;

public enum AutoPickupMode {
    OFF,
    MATCHING_ONLY,
    PICKUP_ALL;

    public AutoPickupMode next() {
        return switch (this) {
            case OFF -> MATCHING_ONLY;
            case MATCHING_ONLY -> PICKUP_ALL;
            case PICKUP_ALL -> OFF;
        };
    }

    public String displayName() {
        return switch (this) {
            case OFF -> "Off";
            case MATCHING_ONLY -> "Matching Only";
            case PICKUP_ALL -> "Pick Up All";
        };
    }
}