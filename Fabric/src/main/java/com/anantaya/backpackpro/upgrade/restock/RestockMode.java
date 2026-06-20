package com.anantaya.backpackpro.upgrade.restock;

public enum RestockMode {
    OFF("Off"),
    SMART("Smart");

    private final String displayName;

    RestockMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public RestockMode next() {
        return switch (this) {
            case OFF -> SMART;
            case SMART -> OFF;
        };
    }

    public static RestockMode byName(String name) {
        for (RestockMode mode : values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }

        return SMART;
    }
}