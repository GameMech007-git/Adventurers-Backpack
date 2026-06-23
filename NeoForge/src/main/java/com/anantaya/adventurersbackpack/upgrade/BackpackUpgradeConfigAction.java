package com.anantaya.adventurersbackpack.upgrade;

public enum BackpackUpgradeConfigAction {
    CYCLE_AUTO_PICKUP_MODE,
    TOGGLE_IGNORE_PLAYER_DROPS,

    CYCLE_FOOD_POUCH_MODE,
    TOGGLE_FOOD_POUCH_EMPTY_WARNING,

    TOGGLE_RESTOCK_TOOLS,
    TOGGLE_RESTOCK_BUCKETS,

    SET_RECALL_HOME,
    TELEPORT_RECALL_HOME,

    SET_RECALL_WAYPOINT_1,
    TELEPORT_RECALL_WAYPOINT_1,

    SET_RECALL_WAYPOINT_2,
    TELEPORT_RECALL_WAYPOINT_2;

    public static BackpackUpgradeConfigAction byId(int id) {
        BackpackUpgradeConfigAction[] values = values();

        if (id < 0 || id >= values.length) {
            return CYCLE_AUTO_PICKUP_MODE;
        }

        return values[id];
    }

    public int id() {
        return ordinal();
    }
}