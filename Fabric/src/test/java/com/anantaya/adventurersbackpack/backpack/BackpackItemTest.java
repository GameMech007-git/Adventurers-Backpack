package com.anantaya.adventurersbackpack.backpack;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.minecraft.world.InteractionResult;

public class BackpackItemTest {

    @Test
    public void sanitizeDurabilityClampsNegativeValues() {
        assertEquals(0, BackpackDurability.sanitizeDurability(-10, 20));
    }

    @Test
    public void sanitizeDurabilityClampsAboveMaximum() {
        assertEquals(20, BackpackDurability.sanitizeDurability(25, 20));
    }

    @Test
    public void sanitizeDurabilityPreservesValidValues() {
        assertEquals(10, BackpackDurability.sanitizeDurability(10, 20));
    }

    @Test
    public void upgradeItemsUseUpgradeSlotsFirst() {
        int[][] ranges = BackpackMenuTransferHelper.getPreferredTransferRangesForTest(
                true,
                BackpackTier.IRON,
                3,
                true
        );

        assertEquals(1, ranges.length);
        assertArrayEquals(
                new int[]{BackpackTier.IRON.upgradeStart(), BackpackTier.IRON.upgradeStart() + BackpackTier.IRON.upgradeSlots},
                ranges[0]
        );
    }

    @Test
    public void normalItemsPreferStorageBeforeExtraStorage() {
        int[][] ranges = BackpackMenuTransferHelper.getPreferredTransferRangesForTest(
                false,
                BackpackTier.IRON,
                3,
                true
        );

        assertEquals(3, ranges.length);
        assertArrayEquals(new int[]{0, BackpackTier.IRON.protectedSlots}, ranges[0]);
        assertArrayEquals(new int[]{BackpackTier.IRON.normalStart(), BackpackTier.IRON.upgradeStart()}, ranges[1]);
        assertArrayEquals(new int[]{BackpackTier.IRON.totalSlots, BackpackTier.IRON.totalSlots + 3}, ranges[2]);
    }

    @Test
    public void playerInventoryMappingIncludesBottomRowButNotHotbar() {
        BackpackMenuTransferHelper helper = new BackpackMenuTransferHelper(
                null,
                BackpackTier.IRON,
                null,
                0,
                null
        );

        assertEquals(0, helper.playerInventoryMenuIndex(9));
        assertEquals(26, helper.playerInventoryMenuIndex(35));
        assertEquals(-1, helper.playerInventoryMenuIndex(8));
    }

    @Test
    public void blockPlacementDoesNotOpenBackpackMenu() {
        assertFalse(BackpackPlacementDecision.shouldOpenBackpackMenu(InteractionResult.SUCCESS));
        assertFalse(BackpackPlacementDecision.shouldOpenBackpackMenu(InteractionResult.CONSUME));
        assertTrue(BackpackPlacementDecision.shouldOpenBackpackMenu(InteractionResult.PASS));
        assertTrue(BackpackPlacementDecision.shouldOpenBackpackMenu(InteractionResult.FAIL));
    }
}

