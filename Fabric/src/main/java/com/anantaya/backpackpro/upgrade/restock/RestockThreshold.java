package com.anantaya.backpackpro.upgrade.restock;

public enum RestockThreshold {
    LOW_25("25%", 25),
    NORMAL_50("50%", 50),
    HIGH_75("75%", 75);

    private final String displayName;
    private final int percent;

    RestockThreshold(String displayName, int percent) {
        this.displayName = displayName;
        this.percent = percent;
    }

    public String displayName() {
        return displayName;
    }

    public int percent() {
        return percent;
    }

    public RestockThreshold next() {
        return switch (this) {
            case LOW_25 -> NORMAL_50;
            case NORMAL_50 -> HIGH_75;
            case HIGH_75 -> LOW_25;
        };
    }

    public static RestockThreshold byName(String name) {
        for (RestockThreshold threshold : values()) {
            if (threshold.name().equals(name)) {
                return threshold;
            }
        }

        return NORMAL_50;
    }
}