package com.anantaya.backpackpro.backpack;

public final class BackpackMenuLayout {

    public static final int MAIN_WIDTH = 176;
    public static final int SIDE_WIDTH = 28;
    public static final int GUI_WIDTH = MAIN_WIDTH + SIDE_WIDTH;

    public static final int SLOT_SIZE = 18;

    // ===== Content frame =====
    public static final int CONTENT_BOX_X = 6;
    public static final int CONTENT_BOX_WIDTH = 164;

    public static final int CONTENT_INNER_X = 7;

    // ===== Tiny utility buttons (top-right) =====
    public static final int TOP_BUTTON_Y = 6;
    public static final int TOP_BUTTON_SIZE = 10;
    public static final int TOP_BUTTON_GAP = 2;

    // place the 3 buttons in the empty top-right area
    public static final int UP_BUTTON_X = 156;
    public static final int DOWN_BUTTON_X = UP_BUTTON_X - TOP_BUTTON_SIZE - TOP_BUTTON_GAP;   // 144
    public static final int SORT_BUTTON_X = DOWN_BUTTON_X - TOP_BUTTON_SIZE - TOP_BUTTON_GAP; // 132

    public static final int PLAYER_INV_X = CONTENT_INNER_X;

    public static final int UPGRADE_SLOT_X = 184;
    public static final int UPGRADE_SLOT_Y = 23;

    public final BackpackTier tier;

    public final int magicX;
    public final int magicY;

    public final int normalX;
    public final int normalY;

    public final int playerInvX;
    public final int playerInvY;

    public final int hotbarX;
    public final int hotbarY;

    public final int guiWidth;
    public final int guiHeight;

    private BackpackMenuLayout(BackpackTier tier) {
        this.tier = tier;

        // keep the top area compact
        this.magicY = 22;
        this.magicX = centeredX(Math.min(tier.protectedSlots, 9));

        this.normalX = CONTENT_INNER_X;
        this.normalY = this.magicY + tier.protectedRows() * SLOT_SIZE + 9;

        this.playerInvX = PLAYER_INV_X;
        this.playerInvY = this.normalY + tier.normalRows() * SLOT_SIZE + 10;

        this.hotbarX = PLAYER_INV_X;
        this.hotbarY = this.playerInvY + 3 * SLOT_SIZE + 4;

        this.guiWidth = GUI_WIDTH;
        this.guiHeight = this.hotbarY + SLOT_SIZE + 7;
    }

    public static BackpackMenuLayout of(BackpackTier tier) {
        return new BackpackMenuLayout(tier);
    }

    public int magicSlotX(int index) {
        int row = index / 9;
        int col = index % 9;

        int slotsInRow = Math.min(9, tier.protectedSlots - row * 9);
        int rowX = centeredX(slotsInRow);

        return rowX + col * SLOT_SIZE;
    }

    public int magicSlotY(int index) {
        int row = index / 9;
        return this.magicY + row * SLOT_SIZE;
    }

    public int normalSlotX(int index) {
        int col = index % 9;
        return this.normalX + col * SLOT_SIZE;
    }

    public int normalSlotY(int index) {
        int row = index / 9;
        return this.normalY + row * SLOT_SIZE;
    }

    public int playerInvSlotX(int col) {
        return this.playerInvX + col * SLOT_SIZE;
    }

    public int playerInvSlotY(int row) {
        return this.playerInvY + row * SLOT_SIZE;
    }

    public int hotbarSlotX(int index) {
        return this.hotbarX + index * SLOT_SIZE;
    }

    public int upgradeSlotX() {
        return UPGRADE_SLOT_X;
    }

    public int upgradeSlotY(int index) {
        return UPGRADE_SLOT_Y + index * SLOT_SIZE;
    }

    public int trashSlotX() {
        return upgradeSlotX();
    }

    public int trashSlotY() {
        return upgradeSlotY(BackpackTier.MAX_UPGRADE_SLOTS);
    }

    public static int centeredX(int slotCount) {
        return (MAIN_WIDTH - slotCount * SLOT_SIZE) / 2;
    }

    public static final int LEFT_PANEL_GAP = 6;

    public static final int FLUID_PANEL_WIDTH = 30;
    public static final int FLUID_PANEL_X = -36;

    public static final int EXTRA_STORAGE_COLS = 3;
    public static final int EXTRA_STORAGE_PANEL_X =
            FLUID_PANEL_X - LEFT_PANEL_GAP - extraStoragePanelWidthStatic();

    public static int extraStorageSlotsForTier(BackpackTier tier) {
        return switch (tier) {
            case IRON -> 15;
            case DIAMOND -> 24;
            case NETHERITE -> 36;
        };
    }

    private static int extraStoragePanelWidthStatic() {
        return EXTRA_STORAGE_COLS * SLOT_SIZE + 8;
    }

    public int extraStorageSlots() {
        return extraStorageSlotsForTier(this.tier);
    }

    public int extraStorageRows() {
        return (int) Math.ceil(extraStorageSlots() / (double) EXTRA_STORAGE_COLS);
    }

    public int extraStoragePanelX() {
        return EXTRA_STORAGE_PANEL_X;
    }

    public int extraStoragePanelY() {
        return 0;
    }

    public int extraStoragePanelWidth() {
        return EXTRA_STORAGE_COLS * SLOT_SIZE + 8;
    }

    public int extraStoragePanelHeight() {
        return extraStorageRows() * SLOT_SIZE + 8;
    }

    public int extraStorageSlotX(int index) {
        int col = index % EXTRA_STORAGE_COLS;
        return extraStoragePanelX() + 4 + col * SLOT_SIZE;
    }

    public int extraStorageSlotY(int index) {
        int row = index / EXTRA_STORAGE_COLS;
        return extraStoragePanelY() + 4 + row * SLOT_SIZE;
    }

    public int craftingPanelX() {
        return upgradeSlotX() - 3;
    }

    public int craftingPanelY() {
        return upgradeSlotY(0) + 130;
    }

    public int craftingPanelWidth() {
        return 90;
    }

    public int craftingPanelHeight() {
        return 64;
    }

    public int craftingGridX() {
        return craftingPanelX() + 6;
    }

    public int craftingGridY() {
        return craftingPanelY() + 5;
    }

    public int craftingResultX() {
        return craftingGridX() + 60;
    }

    public int craftingResultY() {
        return craftingGridY() + 18;
    }
}