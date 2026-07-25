package com.anantaya.adventurersbackpack.backpack;

import com.anantaya.adventurersbackpack.menu.BackpackUnloadHelper;
import com.anantaya.adventurersbackpack.menu.BackpackUnloadTargetHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class BackpackItem extends BlockItem {

    private static final String DURABILITY_KEY = "BackpackDurability";

    private final BackpackTier tier;

    public BackpackItem(BackpackTier tier, Block block, Properties properties) {
        super(block, properties);
        this.tier = tier;
    }

    public BackpackTier getTier() {
        return tier;
    }


    public static int sanitizeDurability(int durability, int maxDurability) {
        return BackpackDurability.sanitizeDurability(durability, maxDurability);
    }

    public static int getDurability(ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return 0;
        }

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = (data != null) ? data.copyTag() : new CompoundTag();

        if (!tag.contains(DURABILITY_KEY)) {
            BackpackTier t = backpackItem.tier;
            int defaultDurability = sanitizeDurability(t.maxDurability, t.maxDurability);
            tag.putInt(DURABILITY_KEY, defaultDurability);

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            return defaultDurability;
        }

        int rawDurability = tag.getInt(DURABILITY_KEY).orElse(0);
        int sanitizedDurability = sanitizeDurability(rawDurability, backpackItem.tier.maxDurability);

        if (rawDurability != sanitizedDurability) {
            tag.putInt(DURABILITY_KEY, sanitizedDurability);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        return sanitizedDurability;
    }

    public static void setDurability(ItemStack stack, int value) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        CustomData data = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        );

        CompoundTag tag = data.copyTag();
        tag.putInt(DURABILITY_KEY, sanitizeDurability(value, backpackItem.tier.maxDurability));

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }


    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getDurability(stack) < tier.maxDurability;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxDurability = Math.max(1, tier.maxDurability);
        return Math.round(13.0f * getDurability(stack) / maxDurability);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int maxDurability = Math.max(1, tier.maxDurability);
        float fraction = (float) getDurability(stack) / maxDurability;

        int r = (int) Math.min(255, 255 * (1.0f - fraction) * 2);
        int g = (int) Math.min(255, 255 * fraction * 2);

        return (r << 16) | (g << 8);
    }


    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        int dur = getDurability(stack);
        int maxDur = Math.max(1, tier.maxDurability);

        ChatFormatting colour = dur <= 1 ? ChatFormatting.RED
                : dur <= maxDur / 2 ? ChatFormatting.YELLOW
                : ChatFormatting.GREEN;

        tooltip.accept(
                Component.literal("Durability: " + dur + " / " + maxDur)
                        .withStyle(colour)
        );

        tooltip.accept(
                Component.literal("Each death consumes 1 durability")
                        .withStyle(ChatFormatting.GRAY)
        );
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        if (player != null && context.isSecondaryUseActive()) {
            Level level = context.getLevel();

            Container target = BackpackUnloadTargetHelper.resolveTargetContainer(
                    level,
                    context.getClickedPos()
            );

            if (target != null) {
                if (!level.isClientSide()) {
                    ItemStack stack = context.getItemInHand();

                    BackpackUnloadHelper.unloadBackpackToContainer(
                            stack,
                            target,
                            this.tier,
                            level.registryAccess()
                    );
                }

                return level.isClientSide()
                        ? InteractionResult.SUCCESS
                        : InteractionResult.SUCCESS_SERVER;
            }

            return super.useOn(context);
        }

        if (!context.getLevel().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = context.getItemInHand();

            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Backpack");
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player p) {
                    return new BackpackScreenHandler(
                            syncId,
                            playerInv,
                            stack,
                            tier,
                            p.level().registryAccess(),
                            findBackpackSourceSlot(playerInv, stack)
                    );
                }
            });
        }

        return context.getLevel().isClientSide()
                ? InteractionResult.SUCCESS
                : InteractionResult.SUCCESS_SERVER;
    }

    private static int findBackpackSourceSlot(Inventory inventory, ItemStack targetStack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack == targetStack) {
                return i;
            }
        }

        return -1;
    }



    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Backpack");
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player p) {
                    return new BackpackScreenHandler(
                            syncId,
                            playerInv,
                            stack,
                            tier,
                            p.level().registryAccess(),
                            findBackpackSourceSlot(playerInv, stack)
                    );
                }
            });
        }

        return level.isClientSide()
                ? InteractionResult.SUCCESS
                : InteractionResult.SUCCESS_SERVER;
    }
}