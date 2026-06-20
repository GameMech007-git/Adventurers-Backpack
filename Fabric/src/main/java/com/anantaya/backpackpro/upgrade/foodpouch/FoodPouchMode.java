package com.anantaya.backpackpro.upgrade.foodpouch;

public enum FoodPouchMode {
    OFF,
    SMART,
    LOW_VALUE,
    EMERGENCY;

    public FoodPouchMode next() {
        return switch (this) {
            case OFF -> SMART;
            case SMART -> LOW_VALUE;
            case LOW_VALUE -> EMERGENCY;
            case EMERGENCY -> OFF;
        };
    }

    public String displayName() {
        return switch (this) {
            case OFF -> "Off";
            case SMART -> "Smart";
            case LOW_VALUE -> "Low Value";
            case EMERGENCY -> "Emergency";
        };
    }
}