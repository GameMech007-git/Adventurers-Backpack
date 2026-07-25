package com.anantaya.adventurersbackpack.backpack;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
        ItemStack stack = new ItemStack(
                new BackpackUpgradeItem(
                        BackpackUpgradeItem.Type.AUTO_PICKUP,
                        new Item.Properties()
                )
        );

        int[][] ranges = BackpackMenuTransferHelper.getPreferredTransferRanges(
                stack,
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
        int[][] ranges = BackpackMenuTransferHelper.getPreferredTransferRanges(
                new ItemStack(Items.STONE),
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
}
